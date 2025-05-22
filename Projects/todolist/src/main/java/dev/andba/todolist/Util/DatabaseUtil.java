package dev.andba.todolist.Util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DatabaseUtil {

    private static EntityManagerFactory emf;
    private static EntityManager em;

    public static void initialize() {
        // Crea EntityManagerFactory a partire dal persistence-unit 'todoPU'
        emf = Persistence.createEntityManagerFactory("ToDoListPU");

        // Crea EntityManager
        em = emf.createEntityManager();
    }

    public static EntityManager getEntityManager() {
        if (em == null) {
            initialize();
        }
        return em;
    }

    public static void close() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }
}