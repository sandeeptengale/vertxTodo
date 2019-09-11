package model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@DataObject(generateConverter = true)
public class Todo {
    private static final AtomicInteger acc = new AtomicInteger(0);

    private int id;
    private String title;
    private Boolean completed;
    private String url;

    public Todo() {}

    public Todo(int id, String title, Boolean completed, String url) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.url = url;
    }

    public Todo(String json) {
        Todo todo = Json.decodeValue(json, Todo.class);
        this.title = todo.getTitle();
        this.completed = todo.getCompleted();
        this.url = todo.getUrl();
    }

    public Todo(JsonObject jsonObject) {
        this.title = jsonObject.getString("title");
        this.completed = jsonObject.getBoolean("completed");
        this.url = jsonObject.getString("url");
    }

    public static AtomicInteger getAcc() {
        return acc;
    }

    public int getId() {
        return id;
    }

    public void setId() {
        this.id = acc.incrementAndGet();
    }

    public static int getAccId() {
        return acc.get();
    }

    public static void setAccId(int n) {
        acc.set(n);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return id == todo.id &&
                Objects.equals(title, todo.title) &&
                Objects.equals(completed, todo.completed) &&
                Objects.equals(url, todo.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, completed, url);
    }
}
