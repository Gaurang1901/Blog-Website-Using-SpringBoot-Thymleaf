package com.gaurang.blog.website.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.gaurang.blog.website.models.Account;
import com.gaurang.blog.website.models.Comments;
import com.gaurang.blog.website.models.Post;
import com.gaurang.blog.website.services.AccountService;
import com.gaurang.blog.website.services.PostService;
import jakarta.validation.Valid;

@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/posts/{id}")
    public String getPost(@PathVariable Long id, Model model, Principal principal) {
        Optional<Post> optionalPost = postService.getById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("post", post);

            // Get username of the current logged-in session user
            String authUser = null;
            if (principal != null) {
                authUser = principal.getName();
            }

            // Check if the logged-in user is the owner of the post
            boolean isOwner = (authUser != null && authUser.equals(post.getAccount().getEmail()));
            model.addAttribute("isOwner", isOwner);

            // Determine if the user has liked the post
            boolean userHasLiked = post.getLikes().contains(authUser); // You need to manage this in your Post model
            model.addAttribute("userHasLiked", userHasLiked);

            return "post_views/post";
        } else {
            return "404";
        }
    }

    @PostMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public String likePost(@PathVariable Long postId, Principal principal) {
        Optional<Post> optionalPost = postService.getById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            String authUser = principal.getName();

            // Check if the user has already liked the post
            boolean userHasLiked = post.getLikes().contains(authUser);
            if (userHasLiked) {
                postService.unlikePost(post, authUser);
                post.getLikes().remove(authUser); // Update the list of users who liked the post
            } else {
                postService.likePost(post, authUser);
                post.getLikes().add(authUser); // Update the list of users who liked the post
            }
            postService.save(post);
        }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public String addComment(@PathVariable Long postId, @Valid @ModelAttribute Comments comment,
            BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "post_views/post";
        }
        comment.setUser(accountService.findOneByEmail(principal.getName()).orElse(null));
        postService.addComment(postId, comment); // Ensure this method correctly associates the comment with the post
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/delete")
@PreAuthorize("isAuthenticated()")
public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId, Principal principal) {
    Optional<Post> optionalPost = postService.getById(postId);
    if (optionalPost.isPresent()) {
        Post post = optionalPost.get();
        Comments commentToDelete = post.getComments().stream()
            .filter(c -> c.getId().equals(commentId))
            .findFirst()
            .orElse(null);
        if (commentToDelete != null) {
            post.getComments().remove(commentToDelete);
            postService.save(post);
        }
    }
    return "redirect:/posts/" + postId;
}


    @GetMapping("/posts/add")
    @PreAuthorize("isAuthenticated()")
    public String addPost(Model model, Principal principal) {
        String authUser = "email";
        if (principal != null) {
            authUser = principal.getName();
        }
        Optional<Account> optionalAccount = accountService.findOneByEmail(authUser);
        if (optionalAccount.isPresent()) {
            Post post = new Post();
            post.setAccount(optionalAccount.get());
            model.addAttribute("post", post);
            return "post_views/post_add";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/posts/add")
    @PreAuthorize("isAuthenticated()")
    public String addPostHandler(@Valid @ModelAttribute Post post, BindingResult bindingResult, Principal principal) {

        if (bindingResult.hasErrors()) {
            return "post_views/post_add";
        }
        String authUser = "email";
        if (principal != null) {
            authUser = principal.getName();
        }
        if (post.getAccount().getEmail().compareToIgnoreCase(authUser) < 0) {
            return "redirect:/?error";
        }
        postService.save(post);
        return "redirect:/posts/" + post.getId();
    }

    @GetMapping("/posts/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String getPostForEdit(@PathVariable Long id, Model model) {
        Optional<Post> optionaPost = postService.getById(id);
        if (optionaPost.isPresent()) {
            Post post = optionaPost.get();
            model.addAttribute("post", post);
            return "post_views/post_edit";
        } else {
            return "404";
        }
    }

    @PostMapping("/posts/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updatePost(@Valid @ModelAttribute Post post, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            return "post_views/post_edit";
        }

        Optional<Post> optionalPost = postService.getById(id);
        if (optionalPost.isPresent()) {
            Post existingPost = optionalPost.get();
            existingPost.setTitle(post.getTitle());
            existingPost.setBody(post.getBody());
            postService.save(existingPost);
        }
        return "redirect:/posts/" + post.getId();

    }

    @GetMapping("/posts/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deletePost(@PathVariable Long id) {
        Optional<Post> optionalPost = postService.getById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            postService.delete(post);
            return "redirect:/";
        } else {
            return "redirect:/?error";
        }

    }

}
