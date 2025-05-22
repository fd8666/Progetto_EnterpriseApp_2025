package org.example.enterpriceappbackend.configuration.filter;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomRequestWrapper extends HttpServletRequestWrapper {

    private final String modificarichiesteURI;
    private final Map<String,String[]> modificaParametri;

    public CustomRequestWrapper(HttpServletRequest request) {
        super(request);
        this.modificarichiesteURI = request.getRequestURI().replaceAll("%20"," ");
        this.modificaParametri = modifyParameters(request.getParameterMap());
        log.info("Richiesta URI : {}", this.modificarichiesteURI);
        this.modificaParametri.forEach((k,v) -> {log.info("Richiesta parametri: {} = {}", k,v);});
    }

    @Override
    public String getRequestURI() {
        return this.modificarichiesteURI;
    }

    @Override
    public String getParameter(String name) {
        String[] values = modificaParametri.get(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return modificaParametri;
    }

    @Override
    public String[] getParameterValues(String name) {
        return modificaParametri.get(name);
    }

    private Map<String,String[]> modifyParameters(Map<String,String[]> map) {
        Map<String,String[]> newMap = new HashMap<>();
        for(Map.Entry<String,String[]> entry : map.entrySet()) {
            String key = entry.getKey().replaceAll("%20"," ");
            String[] values = entry.getValue();
            String[] newValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                newValues[i] = values[i].replaceAll("%20"," ");
            }
            newMap.put(key, newValues);
        }
        return newMap;
    }


}
