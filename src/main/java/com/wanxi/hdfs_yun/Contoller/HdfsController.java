package com.wanxi.hdfs_yun.Contoller;

import com.wanxi.hdfs_yun.Service.HdfsSrevice;
import com.wanxi.hdfs_yun.pojo.FileIndex;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import static com.wanxi.hdfs_yun.pojo.BuildMethods.breadCrumb;

@Controller
public class HdfsController {
    private HdfsSrevice hdfsSrevice;

    @Autowired
    public HdfsController(HdfsSrevice hdfsSrevice) {
        this.hdfsSrevice = hdfsSrevice;
    }

    private String loginUserId;
    @Value("${hadoop.localpath}")
    private String localpath;

    /**
     * 登入界面渲染
     *
     * @return index
     */
    @GetMapping("/index")
    public String show() {
        return "index";
    }

    /**
     * 登入判断
     */
    @PostMapping("/index")
    public String login(@RequestParam String userid, @RequestParam String userpwd, RedirectAttributes attributes) throws IOException, URISyntaxException, InterruptedException {
        //验证身份
        if (hdfsSrevice.login(userid, userpwd)) {
            loginUserId = userid;
            return "redirect:/listfile?path=" + userid;
        } else {
            attributes.addFlashAttribute("message", "账号或密码错误");
            return "redirect:/index";
        }
    }

    @GetMapping("/register")
    public String show1() {
        return "register";
    }

    @PostMapping("/register")
    public String register(String userId, String userName, String userPwd, String userPwdAgain,
                           RedirectAttributes attributes) throws IOException, URISyntaxException, InterruptedException {
        // 检验两次密码是否一致
        if (!userPwd.equals(userPwdAgain)) {
            attributes.addFlashAttribute("message", "两次密码不一致");
            return "redirect:/register";
        }
        // 检验用户名是否存在
        if (hdfsSrevice.userIdExist(userId)) {
            attributes.addFlashAttribute("message", "用户id与他人重复！");
            return "redirect:/register";
        }
        // 添加用户信息到userinfo.dat 文件中
        if (hdfsSrevice.insertUser(userId, userName, userPwd)) {
            attributes.addFlashAttribute("message", "注册成功！");
        } else attributes.addFlashAttribute("message", "注册失败");
        return "redirect:/index";
    }

    //查showfile 增mkdir 删delete 改revise=============================================

    @GetMapping("/listfile")
    public String showfile(@RequestParam("path") String path, RedirectAttributes attributes, Model model) throws IOException, URISyntaxException, InterruptedException {
        if (loginUserId == null) {
            attributes.addFlashAttribute("message", "请登入！");
            return "redirect:/index";
        }
        // path=1231/demo/ee/aa.png
        Path hdfspath;
        String bcpath;
        // 判断是否是目录
        Path hdfsurl = new Path("/home/" + path); // hdfsurl=/home/1231/demo/ee/aa.png
        if (hdfsSrevice.isFile(hdfsurl)) {
            //是文件
            hdfspath = hdfsurl.getParent();
            int lastIndex = path.lastIndexOf('/');
            bcpath = path.substring(0, lastIndex);
        } else {
            //不是文件
            hdfspath = hdfsurl;
            bcpath = path;
        }
        List<FileIndex> list = hdfsSrevice.listFlie(hdfspath);  // hdfspath=/home/1231/demo/ee
        List<List<String>> urlList = breadCrumb(bcpath);
        //面包屑
        model.addAttribute("userpath", urlList);
        //用户ID
        model.addAttribute("userid", loginUserId);
        //当前路径（后面加name传过来）
        model.addAttribute("nowpath", bcpath);
        //文件展示
        model.addAttribute("list", list);
        return "listfile";
    }

    /**
     * 新建文件夹
     * @return
     */
    @GetMapping("/mkdir")
    public String addDir(@RequestParam("dirpath") String path, RedirectAttributes attributes, Model model) throws IOException, URISyntaxException, InterruptedException {
        if (loginUserId == null) {
            attributes.addFlashAttribute("message", "请登入！");
            return "redirect:/index";
        }
        System.out.println("数据传过来了");
//        path=/dae/daeda/dir/o
        //文件夹在hdfs中的路径：
        hdfsSrevice.mkdir(new Path("/home/" + path));
        //文件名不能包含"/"，待完善
        int lastIndex = path.lastIndexOf('/');
        //没有找到/时处理没加，待完善
        String fpath = path.substring(0, lastIndex);
        return "redirect:/listfile?path=" + fpath;
    }


    @GetMapping("/deletefile")
    public String delete(@RequestParam("path") String path, RedirectAttributes attributes, Model model) throws IOException, URISyntaxException, InterruptedException {
        if (loginUserId == null) {
            attributes.addFlashAttribute("message", "请登入！");
            return "redirect:/index";
        }
        hdfsSrevice.deleteFile(new Path("/home/" + path));
        //文件名不能包含"/"，待完善
        int lastIndex = path.lastIndexOf('/');
        //没有找到/时处理没加，待完善
        String fpath = path.substring(0, lastIndex);
        return "redirect:/listfile?path=" + fpath;
    }

    /**
     * 移动路径
     *
     * @return
     */
    @GetMapping("/Previse")
    public String revisePath(@RequestParam String path, RedirectAttributes attributes, Model model) throws IOException, URISyntaxException, InterruptedException {
        return null;
    }

    /**
     * 重命名
     *
     * @return
     */
    @GetMapping("/revise")
    public String reviseName(@RequestParam("url") String path, @RequestParam("oldname") String oldname, @RequestParam("newname") String newname,
                             RedirectAttributes attributes, Model model) throws IOException, URISyntaxException, InterruptedException {
        Path oldpath = new Path("/home/" + path + "/" + oldname);
        Path newpath = new Path("/home/" + path + "/" + newname);
        hdfsSrevice.reviseName(oldpath, newpath);
        return "redirect:/listfile?path=" + path;
    }

    /**
     * 上传文件
     *
     * @return
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("hdfspath") String hdfspath, @RequestParam("upfile") MultipartFile localIn,
                         RedirectAttributes attributes) throws IOException, URISyntaxException, InterruptedException {
        String fileName = localIn.getOriginalFilename();
        System.out.println("/home/" + hdfspath + "/" + fileName);
        hdfsSrevice.cloudUpload(localIn, new Path("/home/" + hdfspath + "/" + fileName));
        attributes.addFlashAttribute("message", "上传成功！文件名为：" + fileName);
        //返回
        return "redirect:/listfile?path=" + hdfspath;
    }

    /**
     * 下载文件
     *
     * @return
     */
    @GetMapping("/download")
    public String download(@RequestParam("path") String path, RedirectAttributes attributes) throws IOException, URISyntaxException, InterruptedException {
        hdfsSrevice.download(new Path("/home/" + path), new Path(localpath));
        attributes.addFlashAttribute("message", "下载成功！");
        //返回
        int lastIndex = path.lastIndexOf('/');
        String fpath = path.substring(0, lastIndex);
        return "redirect:/listfile?path=" + fpath;
    }
}
