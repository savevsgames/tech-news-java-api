package com.technews.controller;

import com.technews.exception.ResourceNotFoundException;
import com.technews.model.Post;
import com.technews.model.User;
import com.technews.model.Vote;
import com.technews.repository.PostRepository;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PostController {

    @Autowired
    PostRepository repository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VoteRepository voteRepository;

    @GetMapping("/api/posts")
    public List<Post> getAllPosts() {
        List<Post> postList = repository.findAll();
        for (Post p : postList){
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
        }
        return postList;
    }

    @GetMapping("/api/posts/{id}")
    public Post getPostById(@PathVariable int id) {
        Post post = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + id));
        post.setVoteCount(voteRepository.countVotesByPostId(post.getId()));
        return post;
    }

    @PutMapping("/api/posts/{id}")
    public Post updatePost(@PathVariable int id, @RequestBody Post post) {
        Post tempPost = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + id));
        tempPost.setTitle(post.getTitle());
        return repository.save(tempPost);
    }

    // Because every post can be upvoted - we need to track each post with addVote()
    @PutMapping("/api/posts/upvote")
    public String addVote(@RequestBody Vote vote, HttpServletRequest request) {
        String returnValue = "";
        if (request.getSession(false) == null) {
            returnValue = "login";
            return returnValue; // User must be logged in to vote
        }

        User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
        if (sessionUser == null) {
            returnValue = "login";
            return returnValue; // Double-check that session contains a valid user
        }

        // Set user ID for the vote
        vote.setUserId(sessionUser.getId());
        voteRepository.save(vote);

        // Fetch the post and update the vote count
        Post returnPost = repository.findById(vote.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + vote.getPostId()));

        returnPost.setVoteCount(voteRepository.countVotesByPostId(vote.getPostId()));

        return returnValue; // Return an empty string
    }

    @DeleteMapping("api/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable int id) {
        repository.deleteById(id);
    }



}
