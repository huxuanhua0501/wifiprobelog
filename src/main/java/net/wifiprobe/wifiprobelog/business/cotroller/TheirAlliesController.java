package net.wifiprobe.wifiprobelog.business.cotroller;

import net.wifiprobe.wifiprobelog.business.service.IWifiProbeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class TheirAlliesController {
    @Autowired
    private IWifiProbeService wifiProbeService;

    @GetMapping(value = "theirAllies")
    public String theirAllies(HttpServletResponse response) throws IOException {
        wifiProbeService.basedDataStorage();
        wifiProbeService.theirAllies(response);
        return null;
    }
}
