package ce.mnu.siteuser;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.springframework.http.*;
import org.springframework.core.io.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping(path="/siteuser")
public class SiteUserController {
	@Autowired
	private SiteUserRepository userRepository;
	
	
	@GetMapping(value = {"", "/"})
	public String start(Model model) {
		return "start";
	}
	
	//signup
	@GetMapping(path="/signup")
	public String signup(Model model) {
		model.addAttribute("siteuser", new SiteUser());
		return "signup";
	}
	
	@PostMapping(path="/signup")
	public String signup(@ModelAttribute SiteUser user, Model model) {
		userRepository.save(user);
		model.addAttribute("name", user.getName());
		return "signup_done";
	}

	//find
	@GetMapping(path="/find")
	public String find() {
		return "find_user";
	}
	@PostMapping(path="/find")
	public String findUser(@RequestParam(name="email") String email, HttpSession session, Model model, RedirectAttributes rd) {
		SiteUser user = userRepository.findByEmail(email);
		if(user != null) {
			model.addAttribute("user", user);
			return "find_done";
		}
		rd.addFlashAttribute("reason", "wrong email");
		return "redirect:/error";
	}

	//login
	@GetMapping(path="/login")
	public String loginForm() {
		return "login";
	}
	@PostMapping(path="/login")
	public String loginUser(@RequestParam(name="email") String email, @RequestParam(name="passwd") String passwd, HttpSession session, RedirectAttributes rd) {
		SiteUser user = userRepository.findByEmail(email);
		if(user != null) {
			if(passwd.equals(user.getPasswd())) {
				session.setAttribute("email", email);
				return "login_done";
			}
		}
		rd.addFlashAttribute("reason", "wrong password");
		return "redirect:/error";
	}

	//userMod
	@GetMapping(path="/userMod")
	public String userMod(Model model) {
		return "user_mod";
	}
	
	
	@GetMapping(path="/logout")
	public String logout(HttpSession session) {
		session.invalidate( );
		return "start";
	}
	
	@Autowired
	private ArticleRepository articleRepository;
	@Value("${spring.servlet.multipart.location}")
	private String base; // 파일 저장 폴더 
	
	private String tmpFileName = "";
	
	@GetMapping(path="/bbs/write")
	public String bbsForm(Model model, HttpSession session, RedirectAttributes rd) {
		String email = (String) session.getAttribute("email");
		if (email==null) {
			rd.addFlashAttribute("reason", "login required");
			return "redirect:/error";
		}
		
		model.addAttribute("article", new Article());
		return "new_article";
	}
	
	@PostMapping(path="/bbs/add")
	public String addArticle(@ModelAttribute Article article, Model model, @RequestParam MultipartFile file) throws IllegalStateException, IOException{
		if( ! file.isEmpty( )) {
			String newName = file.getOriginalFilename( );
			newName = newName.replace(' ', '_');
			FileDto dto = new FileDto(newName, file.getContentType( ));
			String[] filenameAry = dto.getFileName().split("\\.");
			
			SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd_HHmmss");
	        String timeStamp = date.format(new Date());
	        tmpFileName=filenameAry[0]+"_"+timeStamp+"."+filenameAry[1];
			File upfile = new File(filenameAry[0]+"_"+timeStamp+"."+filenameAry[1]);
			file.transferTo(upfile);
			model.addAttribute("file", dto);
		}
		articleRepository.save(article);
		model.addAttribute("article", article);
		return "saved";
	}
	
	@GetMapping(path="/bbs")
	public String getAllArticles(Model model, HttpSession session, RedirectAttributes rd) {
		String email = (String) session.getAttribute("email");
		if (email==null) {
			rd.addFlashAttribute("reason", "login required");
			return "redirect:/error";
		}
		Iterable<Article> data = articleRepository.findAll();
		model.addAttribute("articles", data);
		return "articles";
	}
	
	// 파일 업로드
	@PostMapping(path="/upload")
	public String upload(@RequestParam MultipartFile file, Model model) throws IllegalStateException, IOException {
		if( ! file.isEmpty( )) {
			String newName = file.getOriginalFilename( );
			newName = newName.replace(' ', '_');
			FileDto dto = new FileDto(newName, file.getContentType( ));
			String[] filenameAry = dto.getFileName().split("\\.");
			
			SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd_HHmmss");
	        String timeStamp = date.format(new Date());
	        tmpFileName=filenameAry[0]+"_"+timeStamp+"."+filenameAry[1];
			File upfile = new File(filenameAry[0]+"_"+timeStamp+"."+filenameAry[1]);
			file.transferTo(upfile);
			model.addAttribute("file", dto);
		}
		return "result";
 	}
	
	@GetMapping(path="/upload")
	public String visitUpload( ) {
		return "uploadForm";
	}
	
	// 파일 다운로드
	@GetMapping(path="/download")
	public ResponseEntity<Resource> download(@ModelAttribute FileDto dto) throws IOException {
//		Path path = Paths.get(base + "/" + dto.getFileName());
		Path path = Paths.get(base + "/" + tmpFileName);
		String contentType = Files.probeContentType(path);
		HttpHeaders headers = new HttpHeaders( );
		headers.setContentDisposition(ContentDisposition.builder("attachment")
				.filename(dto.getFileName(), StandardCharsets.UTF_8).build( ));
		headers.add(HttpHeaders.CONTENT_TYPE, contentType);
		Resource rsc = new InputStreamResource(Files.newInputStream(path));
		return new ResponseEntity<>(rsc, headers, HttpStatus.OK);
	}
}
