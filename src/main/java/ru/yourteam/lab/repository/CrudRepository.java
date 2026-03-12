// PATH: .\src\main\java\ru\yourteam\lab\repository\CrudRepository.java
package ru.yourteam.lab.repository;

import java.util.List;

public interface CrudRepository<T> {
    T save(T item);
    T findById(long id);
    List<T> findAll();
    void replaceAll(List<T> items);
}