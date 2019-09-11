package service;

import common.Constants;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.Json;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import model.Todo;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class RedisTodoService implements TodoService {

    private Vertx vertx;
    private RedisOptions config;
    private RedisClient client;

    public RedisTodoService(Vertx vertx, RedisOptions config) {
        this.vertx = vertx;
        this.config = config;
        this.client = RedisClient.create(vertx);
    }

    @Override
    public Completable initData() {
        Todo sampleData = new Todo(Math.abs(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)), "Do something", false, "todo/ex");
        return insert(sampleData).ignoreElement();
    }

    @Override
    public Single<Todo> insert(Todo todo) {
        String encoded = Json.encode(todo);
        return client.rxHset(Constants.REDIS_TODO_KEY, String.valueOf(todo.getId()), encoded).map(e -> todo);
    }

    @Override
    public Single<List<Todo>> getAll() {
        return client.rxHvals(Constants.REDIS_TODO_KEY)
                .map(e -> e.stream().map(x -> new Todo((String) x)).collect(Collectors.toList()));

    }

    @Override
    public Maybe<Todo> getTodoById(String todoId) {
        if (Objects.isNull(todoId)) return Maybe.empty();
        return client.rxHget(Constants.REDIS_TODO_KEY, todoId)
                .map(Todo::new);
    }

    @Override
    public Completable delete(String todoId) {
        return client.rxHdel(Constants.REDIS_TODO_KEY, todoId).ignoreElement();
    }

    @Override
    public Completable deleteAll() {
        return client.rxHvals(Constants.REDIS_TODO_KEY).ignoreElement();
    }
}
