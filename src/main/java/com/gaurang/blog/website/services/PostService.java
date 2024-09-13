package com.gaurang.blog.website.services;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.gaurang.blog.website.models.Comments;
import com.gaurang.blog.website.models.Post;
import com.gaurang.blog.website.repositories.CommentRepository;
import com.gaurang.blog.website.repositories.PostRepository;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    public Optional<Post> getById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Page<Post> findAll(int offset, int pageSize, String field) {
        return postRepository.findAll(PageRequest.of(offset, pageSize).withSort(Direction.DESC, field));
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public Post save(Post post) {
        if (post.getId() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

     public void likePost(Post post, String username) {
        Set<String> likes = post.getLikes();
        if (likes == null) {
            likes = new HashSet<>();
        }
        likes.add(username);
        post.setLikes(likes);
        postRepository.save(post);
    }

    public void unlikePost(Post post, String username) {
        Set<String> likes = post.getLikes();
        if (likes != null && likes.contains(username)) {
            likes.remove(username);
            post.setLikes(likes);
            postRepository.save(post);
        }
    }

    public void addComment(Long postId, Comments comment) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            comment.setPost(post);
            commentRepository.save(comment);
        }
    }
}
