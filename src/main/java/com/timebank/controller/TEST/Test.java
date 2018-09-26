package com.timebank.controller.TEST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timebank.controller.sxq.TableRecordsJson;
import com.timebank.controller.yl.UpdateList;
import com.timebank.domain.Reqest;
import com.timebank.domain.ReqestExample;
import com.timebank.domain.UsersExample;
import com.timebank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.timebank.mapper.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class Test {
    @Autowired
    private ReqestMapper reqestMapper;
    @Autowired
    private TypeMapper typeMapper;
    @Autowired
    private RespondMapper respondMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private CommunityMapper communityMapper;

    @RequestMapping("/wzd")
    public String wzd(Model model){
     Hashtable ht=new Hashtable();


//        model.addAllAttributes("message",);
        return "demo_map3";
    }

//    @RequestMapping("/css1")
//    public String wzd11(Model model){
//
//
////        model.addAllAttributes("message",);
//        return "test";
//    }


    @RequestMapping(value = "/css/tj",method = RequestMethod.POST)
    @ResponseBody
    public String uploadImg(@RequestParam(value="img_z") MultipartFile file)throws Exception {
        System.out.println(11111);
        System.out.println(file);
        System.out.println(22222);

        ClassPathResource resource;
        resource = new ClassPathResource("static/img");
        String absPath=resource.getURL().getPath();
        String fileName=file.getOriginalFilename();

        System.out.println(absPath);
        System.out.println(fileName);

        //将用户传上去的图片下载到主机
        BufferedOutputStream outputStream=new BufferedOutputStream(new FileOutputStream(absPath+"/"+fileName));
        outputStream.write(file.getBytes());
        outputStream.flush();
        outputStream.close();

        //将图片的相对路径保存到数据库
        String dboPath=absPath+"/"+fileName;
//        String url = System.getProperty("user.dir")+"/src/images/";


        return "test";
    }

}
