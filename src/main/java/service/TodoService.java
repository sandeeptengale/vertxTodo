package service;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import model.Todo;

import java.util.List;

public interface TodoService {
    Completable initData();
    Single<Todo> insert(Todo todo);
    Single<List<Todo>> getAll();
    Maybe<Todo> getTodoById(String todoId);
    Completable delete(String todoId);
    Completable deleteAll();
}
