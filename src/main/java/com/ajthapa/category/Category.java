package com.ajthapa.category;

import com.ajthapa.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name="categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;

    public Category(Long id, String name, CategoryType type, AppUser appUser) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.appUser = appUser;
    }

    protected Category(){}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CategoryType getType() {
        return type;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }
}
