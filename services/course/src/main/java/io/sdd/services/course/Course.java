package io.sdd.services.course;

import javax.persistence.*;

@Entity
@Table(name="course")
public class Course {
    @Id
    @Column(name="course_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="email", nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
