package com.gaurang.blog.website.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurang.blog.website.models.Comments;

public interface CommentRepository extends JpaRepository<Comments,Long>{
    
}
