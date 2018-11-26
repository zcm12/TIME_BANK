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

//    @RequestMapping(value = "/wzd")
//    public String createRequestByAdmi(String jd,String wd,String savecityName,String Guid,Model model) {
//        return ""
//    }
}
