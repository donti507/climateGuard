package com.climateguard.api.model;

import jakarta.persistence.EntityListeners;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditingEntityAnnotationTest {

    @Test
    void entitiesUseJpaAuditing() throws Exception {
        assertAuditing(Region.class);
        assertAuditing(FloodEvent.class);
        assertAuditing(ClimateObservation.class);
    }

    private void assertAuditing(Class<?> entityClass) throws Exception {
        EntityListeners entityListeners = entityClass.getAnnotation(EntityListeners.class);
        assertNotNull(entityListeners, entityClass.getSimpleName() + " should declare @EntityListeners");

        assertTrue(
                Arrays.asList(entityListeners.value()).contains(AuditingEntityListener.class),
                entityClass.getSimpleName() + " should use AuditingEntityListener"
        );

        assertFieldAnnotation(entityClass, "createdAt", CreatedDate.class);
        assertFieldAnnotation(entityClass, "updatedAt", LastModifiedDate.class);
        assertFieldAnnotation(entityClass, "createdBy", CreatedBy.class);
    }

    private void assertFieldAnnotation(
            Class<?> entityClass,
            String fieldName,
            Class<? extends Annotation> annotationType
    ) throws Exception {
        Field field = entityClass.getDeclaredField(fieldName);
        assertTrue(
                field.isAnnotationPresent(annotationType),
                entityClass.getSimpleName() + "." + fieldName + " should be annotated with " + annotationType.getSimpleName()
        );
    }
}
