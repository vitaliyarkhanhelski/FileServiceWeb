package restservice.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontEndController {

    @ApiOperation(value = "Web Interface for loading files for 'ReadAndCount' and 'ReplaceEverySecond' and download results",
            notes = "Web Interface for loading files for 'ReadAndCount' and 'ReplaceEverySecond' and download results")
    @GetMapping
    public String home(){
        return "index";
    }

    @GetMapping("/readandcount")
    public String readAndCount(){
        return "readandcount";
    }

    @GetMapping("/replaceeverysecond")
    public String replaceEverySecond(){
        return "replaceeverysecond";
    }
}
