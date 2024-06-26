package sesac.JPA.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sesac.JPA.auth.JwtToken;
import sesac.JPA.dto.*;
import sesac.JPA.service.BoardService;
import sesac.JPA.service.EmailService;
import sesac.JPA.service.ReplyService;
import sesac.JPA.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final BoardService boardService;
    private final UserService userService;
    private final ReplyService replyService;
    private final EmailService emailService;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest req) {
        //String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        //System.out.println("main: "+userId);
        System.out.println("mail: "+req.getHeader("Authorization"));
        HttpSession session = req.getSession();
        String sessionId = (String)session.getAttribute("sessionId");
        System.out.println("session " + sessionId);
        ArrayList<BoardDTO> boardList = (ArrayList<BoardDTO>) boardService.getBoardList();
        model.addAttribute("list",boardList);
        //System.out.println(boardList.get(0).getBoardTitle());
        if(sessionId != null) {
            model.addAttribute("isLogin", true);
            model.addAttribute("userid",sessionId);
        } else model.addAttribute("isLogin", false);
        return "Main";
    }

    @GetMapping("/search")
    public String searchContent(@RequestParam String select, @RequestParam String search, Model model){
        ArrayList<BoardDTO> searchList = (ArrayList<BoardDTO>) boardService.getSearchList(select,search);
        model.addAttribute("list",searchList);
        return "search";
    }

    @GetMapping("/loginHome")
    public String loginHome(Model model, HttpServletRequest req) {
        //String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        //String userId = securityContext.getAuthentication().getPrincipal().toString();
        HttpSession session = req.getSession();
        String sessionId = (String)session.getAttribute("sessionId");
        if(sessionId != null) {
            model.addAttribute("isLogin", true);
            model.addAttribute("userid",sessionId);
        } else model.addAttribute("isLogin", false);
        return "login";
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        String check = userService.checkUser(userDTO);
        switch (check) {
            case "verified" :
//                JwtToken token = userService.login(userDTO);
//                System.out.println("in login controller: "+request.getHeader("Authorization"));
//                HttpHeaders headers = new HttpHeaders();
//                headers.set("Authorization",token.getGrantType()+" "+token.getAccessToken());
//                headers.set("Set-Cookie",token.getGrantType()+" "+ token.getRefreshToken());
                HttpSession session = request.getSession();
                session.setAttribute("sessionId", userDTO.getId());
                return ResponseEntity.status(200).body("ok");
            case "notVerified" :
                return ResponseEntity.status(400).body("비밀번호가 틀렸습니다.");
            case "notPresent" :
                return ResponseEntity.status(404).body("회원 정보가 없습니다.");
            default: return ResponseEntity.status(401).body("you need an authentication");
        }
    }

    @GetMapping("/logout")
    @ResponseBody
    public ResponseEntity<Boolean> logout(@RequestParam String userid, HttpServletRequest request) {
        //String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        HttpSession session = request.getSession();
        String sessionId = (String)session.getAttribute("sessionId");
        System.out.println(userid);
        if(sessionId.equals(userid)) {
            session.invalidate();
            return ResponseEntity.status(201).body(true);
        } else return ResponseEntity.status(404).body(false);
    }

    @GetMapping("/signupHome")
    public String signupHome(Model model) {
        return "signup";
    }

    @PostMapping("/mailAuthReq")
    @ResponseBody
    public ResponseEntity<String> mailAuth(@Valid @RequestBody UserDTO userDTO) {
        if(emailService.sendMail(userDTO.getId())) return ResponseEntity.status(201).body("이메일 인증코드를 요청하였습니다.");
        else return ResponseEntity.status(400).body("이메일 인증 요청에 실패했습니다.");
    }

    @PostMapping("/authCodeReq")
    @ResponseBody
    public ResponseEntity<String> mailAuthCheck(@Valid @RequestBody MailAuthDTO mailAuthDTO) {
        if(emailService.authCodeCheck(mailAuthDTO)) return ResponseEntity.status(201).body("이메일 인증에 성공하였습니다.");
        else return ResponseEntity.status(400).body("이메일 인증에 실패했습니다.");
    }

    @PostMapping("/createUser")
    @ResponseBody
    public ResponseEntity<String> signup(@Valid @RequestBody UserDTO userDTO){
        if(userService.addUser(userDTO).equals("notPresent")) return ResponseEntity.status(201).body("회원 가입이 성공하였습니다.");
        else return ResponseEntity.status(400).body("이미 있는 ID 입니다.");
    }

    @GetMapping("/users")
    public String users(Model model, HttpServletRequest req) {
        //String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        //String userId = securityContext.getAuthentication().getPrincipal().toString();
        HttpSession session = req.getSession();
        String sessionId = (String)session.getAttribute("sessionId");
        model.addAttribute("userid",sessionId);
        return "users";
    }

    @PostMapping("/updateUser")
    @ResponseBody
    public ResponseEntity<String> updateUser(@Valid @RequestBody UserDTO userDTO){
        if(userService.updateUser(userDTO)) return ResponseEntity.status(201).body("회원정보가 수정되었습니다.");
        else return ResponseEntity.status(400).body("다른 비밀번호를 입력해 주세요.");
    }

    @PostMapping("/deleteUser")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@Valid @RequestBody UserDTO userDTO, HttpServletRequest req){
        if(userService.checkUser(userDTO).equals("verified")) {
            HttpSession session = req.getSession();
            session.invalidate();
            userService.deleteUser(userDTO);
            return ResponseEntity.status(201).body("회원 탈퇴 되었습니다.");
        } else return ResponseEntity.status(404).body("비밀번호가 틀렸습니다.");
    }

    @GetMapping("/content/{id}")
    public String content(@PathVariable int id, Model model, HttpServletRequest req) {
        BoardDTO targetBoard = boardService.getBoardInfo(id);
        ArrayList<ReplyDTO> replyList = (ArrayList<ReplyDTO>) replyService.getReplyList(id);
        //String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//        String userId = securityContext.getAuthentication().getPrincipal().toString();
        HttpSession session = req.getSession();
        String sessionId = (String)session.getAttribute("sessionId");
        model.addAttribute("userid", Objects.requireNonNullElse(sessionId, "noname"));
        model.addAttribute("board",targetBoard);
        model.addAttribute("list",replyList);
        return "boardContent";
    }

    @GetMapping("/write")
    public String write(Model model, HttpServletRequest req) {
        //String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        HttpSession session = req.getSession();
        String sessionId = (String)session.getAttribute("sessionId");
//        String userId = securityContext.getAuthentication().getPrincipal().toString();
        if(sessionId != null) model.addAttribute("userid", sessionId);
        return "boardWrite";
    }
    @PostMapping("/createBoard")
    public String boardCreate(CreateBoardDTO createBoardDTO){
        boardService.createBoard(createBoardDTO);
        return "redirect:/";
    }

    @GetMapping("/write/{id}")
    public String boardUpdateHome(@PathVariable int id, Model model, HttpServletRequest req) {
        //String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        HttpSession session = req.getSession();
        String sessionId = (String)session.getAttribute("sessionId");
//        String userId = securityContext.getAuthentication().getPrincipal().toString();
        if(sessionId != null){
            BoardDTO targetBoard = boardService.getBoardInfo(id);
            model.addAttribute("userid",sessionId);
            model.addAttribute("board",targetBoard);
        }
        else model.addAttribute("userid",false);
        return "boardWrite";
    }

    @PatchMapping("/updateBoard")
    @ResponseBody
    public ResponseEntity<String> boardUpdate(@RequestBody BoardDTO boardDTO){
        boardService.updateBoard(boardDTO);
        return ResponseEntity.status(201).body("글이 수정되었습니다.");
    }

    @PostMapping("/deleteBoard")
    @ResponseBody
    public ResponseEntity<String> boardDelete(@RequestBody DeleteBoardDTO boardDTO){
        UserDTO check = new UserDTO();
        check.setId(boardDTO.getUserId());
        check.setPw(boardDTO.getPw());
        if(userService.checkUser(check).equals("verified")) {
            boardService.deleteBoard(boardDTO.getBoardId());
            return ResponseEntity.status(201).body("글이 삭제되었습니다.");
        } else return ResponseEntity.status(404).body("비밀번호가 틀렸습니다.");
    }

    @PostMapping("/createReply")
    @ResponseBody
    public ResponseEntity<Object> replyCreate(@RequestBody ReplyDTO replyDTO){
        replyService.createReply(replyDTO);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/deleteReply")
    public ResponseEntity<String> replyDelete(@RequestBody ReplyDTO replyDTO){
        replyService.deleteReply(replyDTO.getReplyId());
        return ResponseEntity.status(204).build();
    }
}
