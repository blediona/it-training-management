package repository;

public interface IRepository<T> {
    T getById(int id);

    T create(T obj);

    T update(T obj);

    boolean delete(int id);

    boolean delete(T obj);
}

