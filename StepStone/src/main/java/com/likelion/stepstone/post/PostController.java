package com.likelion.stepstone.post;

import com.likelion.stepstone.like.LikeService;
import com.likelion.stepstone.like.model.LikeDto;
import com.likelion.stepstone.like.model.LikeEntity;
import com.likelion.stepstone.post.model.PostDto;
import com.likelion.stepstone.post.model.PostEntity;
import com.likelion.stepstone.user.UserService;
import com.likelion.stepstone.user.model.UserDto;
import com.likelion.stepstone.user.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;


@RequestMapping("/post")
@Controller
public class PostController {

    private final PostService postService;

    private final LikeService likeService;
    private final UserService userService;

    public PostController(PostService postService ,LikeService likeService,UserService userService ) {
        this.postService = postService;
        this.likeService = likeService;
        this.userService = userService;
    }

    @GetMapping("/create")
    public String createForm(PostForm postForm) {
        return "/post/form";
    }

    @PostMapping("/create")
    public String create(Principal principal,Model model, PostForm postForm) {

        //유효성 체크
        boolean hasError = false;

        if (postForm.getTitle() == null || postForm.getTitle().trim().length() == 0) {
            model.addAttribute("titleErrorMsg", "제목을 입력해주세요");
            hasError = true;
        }

        if (postForm.getBody() == null || postForm.getBody().trim().length() == 0) {
            model.addAttribute("bodyErrorMsg", "내용을 입력해주세요");
            hasError = true;
        }

        if (hasError) {
            model.addAttribute("postForm", postForm);
            return "post/form";
        }

        UserDto user = userService.getUser(principal.getName());

        PostDto postDto = PostDto.builder()
                .title(postForm.getTitle())
                .body(postForm.getBody())
                .build();

        postService.create(postDto,user);

        return "redirect:/post/list";
    }

    @GetMapping("/list")
    public String list(Principal principal,Model model, @RequestParam(defaultValue = "0") int page ) {

        UserDto user = userService.getUser(principal.getName());  //현재 로그인 한 user
        Page<PostEntity> paging = postService.getList(page , user);
        model.addAttribute("paging", paging);
        return "post/list";
    }

    @PostAuthorize("hasRole('ROLE_ADMIN') or #postForm.userId == authentication.principal.username")
    @GetMapping("/modify/{postCid}")
    public String postModifyGet(@PathVariable long postCid , PostForm postForm) {
        // @Valid PostForm postForm
        PostDto postDto = postService.getPostDto(postCid);

        postForm.setTitle(postDto.getTitle());
        postForm.setBody(postDto.getBody());
        postForm.setUserId(postDto.getUser().getUserId());
        return "post/form";
    }


    @PreAuthorize("hasRole('ROLE_ADMIN') or #postForm.userId == authentication.principal.username")
    @PostMapping("/modify/{postCid}")
    public String postModifyPost(@PathVariable long postCid , PostForm postForm) {

        PostDto postDto = postService.getPostDto(postCid);
        postService.modify(postDto, postForm.getTitle(), postForm.getBody());

        return "redirect:/post/detail/{postCid}";
    }

    @GetMapping("/detail/{postCid}")
    public String detail(Principal principal,Model model, @PathVariable  long postCid) {

        UserDto user = userService.getUser(principal.getName());
        String exist = "notnull";
        String notexist = "null";

        LikeDto likeDto = likeService.getLikeDto(postCid, user);

        if(likeDto != null){
            model.addAttribute("likeEntity",exist);
        }else{
            model.addAttribute("likeEntity",notexist);
        }

        PostDto postDto = postService.getPostDto(postCid);
        model.addAttribute("postEntity", postDto);
        return "post/detail";
    }

    @GetMapping("/delete/{postCid}")
    public String delete( @PathVariable long postCid) {
        PostDto postDto = postService.getPostDto(postCid);

        postService.delete(postDto);

        return "redirect:/post/list";
    }
}
