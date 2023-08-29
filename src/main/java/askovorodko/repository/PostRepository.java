package askovorodko.repository;

import askovorodko.exception.NotFoundException;
import askovorodko.model.Post;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Stub
@Repository
public class PostRepository {

    private final Map<Long, Post> listPostsMap;
    private final AtomicLong counter;

    public PostRepository() {
        listPostsMap = new ConcurrentHashMap<>();
        counter = new AtomicLong();
    }

    public List<Post> all() {
        if (listPostsMap.isEmpty()) {
            return Collections.emptyList();
        } else {
            Collection<Post> values = listPostsMap.values();
            return values.stream()
                    .filter(x -> !x.isRemoved())
                    .toList();
        }
    }

    public Optional<Post> getById(long id) {
        for (Map.Entry<Long, Post> item : listPostsMap.entrySet()) {
            if (item.getKey().equals(id)) {
                if (!item.getValue().isRemoved()) {
                    return Optional.of(item.getValue());
                }
            }
        }
        return Optional.empty();
    }

    public Post save(Post post) {
        if (post.getId() == 0) {
            for (Map.Entry<Long, Post> item : listPostsMap.entrySet()) {
                if ((counter.get() + 1) == item.getKey()) {
                    counter.incrementAndGet();
                }
            }
            post.setId(counter.incrementAndGet());
            listPostsMap.put(post.getId(), post);
        } else {
            if (listPostsMap.get(post.getId()) != null) {
                if (listPostsMap.get(post.getId()).isRemoved()) {
                    throw new NotFoundException();
                }
            }
            listPostsMap.put(post.getId(), post);
        }
        return post;
    }

    public void removeById(long id) {
        if (listPostsMap.containsKey(id)) {
            listPostsMap.get(id).setRemoved(true);
            if (counter.get() > id) {
                counter.set(id - 1);
            }
        } else {
            throw new NotFoundException("Пост с id: " + id + " не найден");
        }
    }
}