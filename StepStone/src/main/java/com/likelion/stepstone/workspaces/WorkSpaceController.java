package com.likelion.stepstone.workspaces;

import com.likelion.stepstone.user.UserService;
import com.likelion.stepstone.user.model.UserDto;
import com.likelion.stepstone.workspaces.model.WorkSpaceDto;
import com.likelion.stepstone.workspaces.model.WorkSpaceEntity;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequestMapping("/workspace")
@Controller
public class WorkSpaceController {

    private final WorkSpaceService workSpaceService;
    private final UserService userService;

    public WorkSpaceController(WorkSpaceService workSpaceService,UserService userService) {
        this.workSpaceService = workSpaceService;
        this.userService = userService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String create(WorkSpaceForm workSpaceForm) {
        return "workspace/workspace_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createPost(Principal principal, Model model, WorkSpaceForm workSpaceForm) {

        //유효성 체크
        boolean hasError = false;

        if (workSpaceForm.getTitle() == null || workSpaceForm.getTitle().trim().length() == 0) {
            model.addAttribute("titleErrorMsg", "제목을 입력해주세요");
            hasError = true;
        }

        if (workSpaceForm.getBody() == null || workSpaceForm.getBody().trim().length() == 0) {
            model.addAttribute("bodyErrorMsg", "내용을 입력해주세요");
            hasError = true;
        }

        if (hasError) {
            model.addAttribute("workSpaceForm", workSpaceForm);
            return "workspace/workspace_form";
        }

//        UserEntity user = userService.getUser(principal.getName());
        UserDto user = userService.getUser(principal.getName());

        WorkSpaceDto workSpaceDto = WorkSpaceDto.builder()
                .title(workSpaceForm.getTitle())
                .body(workSpaceForm.getBody())
                .build();

        workSpaceService.create(workSpaceDto,user);

        return "redirect:/workspace/list";
    }

    @GetMapping("/list")
    public String list(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<WorkSpaceEntity> paging = workSpaceService.getList(page);
        model.addAttribute("paging", paging);
        return "workspace/workspace_list";
    }

    @GetMapping("/modify/{workspaceCid}")
    public String questionModifyGet(@PathVariable long workspaceCid , WorkSpaceForm workSpaceForm) {
       // @Valid WorkSpaceForm workSpaceForm
        WorkSpaceDto workSpaceDto = workSpaceService.getWorkSpaceDto(workspaceCid);

        workSpaceForm.setTitle(workSpaceDto.getTitle());
        workSpaceForm.setBody(workSpaceDto.getBody());
        return "workspace/workspace_form";
    }
    @PostMapping("/modify/{workspaceCid}")
    public String questionModifyPost(@PathVariable long workspaceCid , WorkSpaceForm workSpaceForm) {

        WorkSpaceDto workSpaceDto = workSpaceService.getWorkSpaceDto(workspaceCid);
        workSpaceService.modify(workSpaceDto, workSpaceForm.getTitle(), workSpaceForm.getBody());

        return "redirect:/workspace/detail/{workspaceCid}";
    }

    @GetMapping("/detail/{workspaceCid}")
    public String detail(Model model, @PathVariable  long workspaceCid) {
        WorkSpaceDto workSpaceDto = workSpaceService.getWorkSpaceDto(workspaceCid);
        model.addAttribute("workSpaceEntity", workSpaceDto);
        return "workspace/workspace_detail";
    }

    @GetMapping("/delete/{workspaceCid}")
    public String delete( @PathVariable long workspaceCid) {
        WorkSpaceDto workSpaceDto = workSpaceService.getWorkSpaceDto(workspaceCid);

        workSpaceService.delete(workSpaceDto);

        return "redirect:/workspace/list";
    }

}