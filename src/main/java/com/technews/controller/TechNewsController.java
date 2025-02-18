package com.technews.controller;

import com.technews.exception.ResourceNotFoundException;
import com.technews.model.Comment;
import com.technews.model.Post;
import com.technews.model.User;
import com.technews.model.Vote;
import com.technews.repository.CommentRepository;
import com.technews.repository.PostRepository;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class TechNewsController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    VoteRepository voteRepository;

    @PostMapping("/users/login")
    public String login(@ModelAttribute User user, Model model, HttpServletRequest request) throws Exception {
        if(user.getPassword() == null || user.getPassword().isEmpty() || (user.getEmail() == null || user.getEmail().isEmpty() )) {
            model.addAttribute("notice", "Email address and password must be populated in order to login!");
            return "login";
        }

        User sessionUser = userRepository.findUserByEmail(user.getEmail());

        try {
            // sessionUser is invalid, running .equals() will throw an error
            if (sessionUser == null){

            }
            // We will catch an error and notify client that email address is not recognized
        } catch (NullPointerException e) {
            model.addAttribute("notice", "Email address is not recognized!");
            return "login";
        }

        // Validation
        String sessionPassword = sessionUser.getPassword();
        boolean isPasswordValid = BCrypt.checkpw(user.getPassword(), sessionPassword);
        if (!isPasswordValid) {
            model.addAttribute("notice", "Password is invalid!");
            return "login";
        }

        sessionUser.setLoggedIn(true);
        request.getSession().setAttribute("SESSION_USER", sessionUser);

        return "redirect:/dashboard";
    }

    @PostMapping("/users")
    public String signup(@ModelAttribute User user, Model model, HttpServletRequest request) throws Exception {
        if ((user.getUsername() == null || user.getUsername().isEmpty()) || (user.getEmail() == null || user.getEmail().isEmpty())) {
            model.addAttribute("notice", "Email address and password must be populated in order to login!");
            return "login";
        }

        try {
            // Encryption
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("notice", "Email address is not available! Please choose a different unique email address.");
            return "login";
        }

        User sessionUser = userRepository.findUserByEmail(user.getEmail());

        try {
            if (sessionUser==null){

            }
        } catch (NullPointerException e) {
            model.addAttribute("notice", "User is not recognized!");
            return "login";
        }

        assert sessionUser != null;
        sessionUser.setLoggedIn(true);
        request.getSession().setAttribute("SESSION_USER", sessionUser);

        return "redirect:/dashboard";
    }

    @PostMapping("/posts")
    public String addPostDashboardPage(@ModelAttribute Post post, Model model, HttpServletRequest request) throws Exception {
        if ((post.getTitle() == null || post.getTitle().isEmpty()) || (post.getPostUrl() == null || post.getPostUrl().isEmpty())) {
            return "redirect:/dashboardEmptyTitleAndLink";
        }

        if (request.getSession(false) == null){
            return "redirect:/login";
        } else {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            post.setUserId(sessionUser.getId());
            postRepository.save(post);

            return "redirect:/dashboard";
        }
    }

    @PostMapping("/posts/{id}")
    public String updatePostDashboardPage(@PathVariable int id, Model model, HttpServletRequest request) throws Exception {
        if (request.getSession(false) == null){
            model.addAttribute("user", new User());
            return "redirect:/dashboard";
        } else {
            Post tempPost = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
            tempPost.setTitle(tempPost.getTitle());
            postRepository.save(tempPost);

            return "redirect:/dashboard";
        }
    }

    @PostMapping("/comments")
    public String createCommentCommentsPage(@ModelAttribute Comment comment, Model model, HttpServletRequest request) throws Exception {

        // Debugging comment creation
        System.out.println("Received comment: " + comment);

        if (comment.getCommentText() == null || comment.getCommentText().isEmpty()) {
            return "redirect:/singlePostEmptyComment/" + comment.getPostId();
        } else {
            // user is the owner of the comment
            if (request.getSession(false) != null){
                User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
                comment.setUserId(sessionUser.getId());
                commentRepository.save(comment);
                return "redirect:/post/" + comment.getPostId();
            } else {
                return "login";
            }
        }
    }

    @PutMapping("/posts/upvote")
    public void addVoteCommentsPage(@RequestBody Vote vote, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getSession(false) != null){
            Post returnPost = null;
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            vote.setUserId(sessionUser.getId());
            voteRepository.save(vote);

            returnPost = postRepository.findById(vote.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found for post " + vote.getPostId()));
            returnPost.setVoteCount(voteRepository.countVotesByPostId(returnPost.getId()));
        }
    }
}
