package de.achim.thyme.controller;

//import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import de.achim.thyme.entity.UserThyme;
import de.achim.thyme.repository.UserRepository;

@Controller
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	SpringTemplateEngine templateEngine;
	
	@GetMapping("/signup")
    public String showSignUpForm(UserThyme userThyme) {
        return "add-user";
    }
    
    @PostMapping("/adduser")
    public String addUser(@Valid UserThyme userThyme, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-user";
        }
        
        userRepository.save(userThyme);
        return "redirect:/index";
    }
    
    @GetMapping("/index")
    public String showUserList(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "index";
    }
    
    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        UserThyme userThyme = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", userThyme);
        
        return "update-user";
    }
    
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable("id") long id, @Valid UserThyme userThyme, BindingResult result, Model model) {
        if (result.hasErrors()) {
        	userThyme.setId(id);
            return "update-user";
        }
        
        userRepository.save(userThyme);

        return "redirect:/index";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") long id, Model model) {
        UserThyme userThyme = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        userRepository.delete(userThyme);
        
        return "redirect:/index";
    }
    
    @RequestMapping(value ="/makePDF", produces="application/pdf" )
    public @ResponseBody byte[] makePDF() throws Exception {
    	String html = parseThymeleafTemplate();
    	java.io.ByteArrayOutputStream result = generatePdfFromHtml(html);
        return result.toByteArray();
    }
    
    private String parseThymeleafTemplate() {
        
        Context context = new Context();
        context.setVariable("users", userRepository.findAll());
         Set<ITemplateResolver> templateResolvers = templateEngine.getTemplateResolvers();
         for(ITemplateResolver res: templateResolvers) {
        	 SpringResourceTemplateResolver reser = (SpringResourceTemplateResolver)res;
        	 System.out.println("******************: " +reser.getPrefix());
        	 System.out.println("******************: " +reser.getSuffix());
         }
         //use template without realtive links to other pages
        return templateEngine.process("index2", context);
    }
    /*
    private String xhtmlConvert(String html) throws UnsupportedEncodingException {
        Tidy tidy = new Tidy();
        tidy.setInputEncoding("UTF-8");
        tidy.setOutputEncoding("UTF-8");
        tidy.setXHTML(true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes("UTF-8"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        tidy.parseDOM(inputStream, outputStream);
        return outputStream.toString("UTF-8");
    }
    */
    
    private ByteArrayOutputStream generatePdfFromHtml(String html) throws Exception {
        String outputFolder = System.getProperty("user.home") + File.separator + "thymeleaf2.pdf";
     //   OutputStream outputStream = new FileOutputStream(outputFolder);

        ITextRenderer renderer = new ITextRenderer();
        
        String baseUrl = FileSystems
                .getDefault()
                .getPath("src", "main", "resources","templates")
                .toUri()
                .toURL()
                .toString();
        renderer.setDocumentFromString(html, baseUrl);
        renderer.layout();
        
        
      //  renderer.setDocumentFromString(html);
      //  renderer.layout();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //    OutputStream outputStream = new FileOutputStream("src//test.pdf");
        renderer.createPDF(outputStream);
        outputStream.close();
        return outputStream;
    }

}
