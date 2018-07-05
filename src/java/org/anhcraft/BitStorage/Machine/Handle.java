package org.anhcraft.BitStorage.Machine;

import java.util.*;

/**
 * Copyright (c) by Anh Craft. All rights reserved.
 * Licensed under the apache license v2.0.
 */
public class Handle {
    public static DataViewer a(List<String> file){
        // line
        int a = 0;
        // backspace

        int b = 0;
        // ----------------------------------------------------------------
        // data
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        // section
        String section = "";
        LinkedHashMap<String, Integer> sections = new LinkedHashMap<>();
        LinkedHashMap<Integer, CommentData> oomments = new LinkedHashMap<>();

        /////////////////////////
        // xem xét section
        Boolean followSection = false;
        Boolean followList = false;
        String currentName = "";

        int cmtLine = 0;
        int ctLine = 0;
        for(String s: file){
            a += 1;
            String c = s.trim();
            // comment
            if(s.trim().indexOf("//") == 0){
                oomments.put(cmtLine, new CommentData(s.replaceFirst("//", "").trim(), false, ctLine));
                cmtLine += 1;
            } else if(c.indexOf("-") == 0 && 0 < currentName.length() && followList){
                ctLine += 1;
                followSection = false;
                String x = c(section, currentName);
                String y = c.replace("- ", "");
                if(data.containsKey(x)){
                    ArrayList<String> rd = (ArrayList) data.get(x);
                    rd.add(y);
                    data.put(x, rd);
                } else {
                    List<String> z = new ArrayList<>();
                    z.add(y);
                    data.put(x, z);
                }
            } else {
                ctLine += 1;
                followList = false;
                String[] d = c.split(" > ");
                d[0] = d[0].replaceAll(" >", "");
                // backspace của dòng này
                int e = b(s);
                // vẫn như bth, tức là ngang hàng
                if(e == b){
                    // ở data trước nếu có followSection thì sẽ tắt dj & cũ là null
                    if(0 < currentName.length() && followSection){
                        followSection = false;
                        if(!data.containsKey(c(section, currentName)) && !sections.containsKey(c(section, currentName))) {
                            data.put(c(section, currentName), "");
                        }
                    }
                    // câp nhật backspace
                    b = e;
                    currentName = d[0];
                    // nếu như ko có giá tri
                    if(d.length == 1){
                        if(a == file.size()){
                            data.put(c(section, currentName), "");
                            break;
                        }
                        followSection = true;
                        followList = true;
                    } else {
                        data.put(c(section, currentName), d(d[1]));
                    }
                }
                // dòng này cách nhìu hơn dòng trc
                else if(b < e){
                    // ở data trước nếu có followSection thì sẽ tắt dj & cũ là null
                    if(0 < currentName.length() && followSection){
                        followSection = false;
                        if(!sections.containsKey(c(section, currentName))) {
                            sections.put(c(section, currentName), e-1);
                        }
                    }
                    // cập nhật section
                    section = c(section, currentName);
                    // câp nhật backspace
                    b = e;
                    currentName = d[0];
                    // nếu như ko có giá tri
                    if(d.length == 1){
                        if(a == file.size()){
                            data.put(c(section, currentName), "");
                            break;
                        }
                        followSection = true;
                        followList = true;
                    } else {
                        data.put(c(section, currentName), d(d[1]));
                    }
                }
                // dòng này thụt vào so vs dòng trc, tức là bỏ section
                else if(e < b) {
                    // ở data trước nếu có followSection thì sẽ tắt dj & cũ là null
                    if(0 < currentName.length() && followSection) {
                        followSection = false;
                        if(!data.containsKey(c(section, currentName)) && !sections.containsKey(c(section, currentName))) {
                            data.put(c(section, currentName), "");
                        }
                    }
                    // cập nhật section
                    if(e+1 == b){
                        section = i(section);
                    } else {
                        section = e(sections, e, section);
                    }
                    // câp nhật backspace
                    b = e;
                    currentName = d[0];
                    // nếu như ko có giá tri
                    if(d.length == 1){
                        if(a == file.size()){
                            data.put(c(section, currentName), "");
                            break;
                        }
                        followSection = true;
                        followList = true;
                    } else {
                        data.put(c(section, currentName), d(d[1]));
                    }
                }
            }
        }
        for(String s : sections.keySet()) {
            if(data.containsKey(s)) {
                data.remove(s);
            }
        }
        return new DataViewer(data, file, oomments, sections);
    }

    private static int b(String a){
        int i = 0;
        for(String b : a.split("")){
            if(b.equals(" ")) {
                i += 1;
            } else {
                break;
            }
        }
        return i;
    }

    private static String c(String section, String name){
        if(section.length() == 0){
            return name.replaceAll(" >", "");
        } else {
            return section + "." + name.replaceAll(" >", "");
        }
    }

    private static Object d(String data){
        String d = data.trim();
        if(isInteger(d)){
           return Integer.parseInt(d);
        } else if(isFloat(d)){
            return Float.parseFloat(d);
        } else if(data.toLowerCase().equals("false")){
            return false;
        } else if(data.toLowerCase().equals("true")){
            return true;
        } else {
            return data;
        }
    }

    private static boolean isInteger(String s) {
        Scanner sc = new Scanner(s.trim());
        if(!sc.hasNextInt()) return false;
        // we know it starts with a valid int, now make sure
        // there's nothing left!
        sc.nextInt();
        return !sc.hasNext();
    }

    private static boolean isFloat(String s) {
        Scanner sc = new Scanner(s.trim());
        if(!sc.hasNextFloat()) return false;
        // we know it starts with a valid int, now make sure
        // there's nothing left!
        sc.nextFloat();
        return !sc.hasNext();
    }

    private static String e(LinkedHashMap<String, Integer> sections, int cb, String sc){
        StringBuilder nsc = new StringBuilder(sc);
        if(!sections.containsKey(nsc.toString())){
            return sc;
        }
        int dscb = sections.get(nsc.toString());
        if(cb == 0){
            nsc = new StringBuilder("");
        } else {
            while(true) {
                nsc = new StringBuilder(i(nsc.toString()));
                if(0 < nsc.length()) {
                    dscb -= sections.get(nsc.toString());
                    if(cb == dscb) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return i(nsc.toString());
    }

    public static DataViewer f(LinkedHashMap<String, Object> result){
        return null;
    }

    private static String g(int a){
        StringBuilder r = new StringBuilder();
        for(int i = 0; i < a; i++){
            r.append(" ");
        }
        return r.toString();
    }

    private static Boolean h(){
        return true;
    }

    private static String i(String section){
        String[] a = section.split("\\.");
        if(a.length < 2) {
            return "";
        } else {
            a = Arrays.copyOf(a, a.length - 1);
            StringBuilder c = new StringBuilder();
            for(String b : a){
                c.append(".").append(b);
            }
            return c.toString().replaceFirst("\\.", "");
        }
    }

    public static LinkedHashMap<String, Object> j(LinkedHashMap<String, Object> data, String sc) {
        LinkedHashMap<String, Object> r = new LinkedHashMap<>();
        int i = sc.split("\\.").length;
        for(String a : data.keySet()){
            String xsc = a;
            while(true){
                if(xsc.split("\\.").length == i || xsc.length() < 1){
                    break;
                }
                xsc = i(xsc);
            }
            if(!sc.equals(xsc)){
                r.put(a, data.get(a));
            }
        }
        return r;
    }

    public static LinkedHashMap<String, Integer> k(LinkedHashMap<String, Integer> data, String sc){
        LinkedHashMap<String, Integer> r = new LinkedHashMap<>();
        int i = sc.split("\\.").length;
        for(String a : data.keySet()){
            String xsc = a;
            while(true){
                if(xsc.split("\\.").length == i || xsc.length() < 1){
                    break;
                }
                xsc = i(xsc);
            }
            if(!sc.equals(xsc)){
                r.put(a, data.get(a));
            }
        }
        return r;
    }

    public static List<String> m(DataViewer d){
        LinkedHashMap<String, Object> result = d.getData();
        LinkedHashMap<Integer, CommentData> comments = d.getComments();
        LinkedHashMap<String, Integer> sections = d.getSections();
        LinkedHashMap<String, List<String>> scdata = new LinkedHashMap<>();
        sections.put("", 0);
        for(String a : sections.keySet()){
            String bs = g(sections.get(a));
            String name = a;
            if(1 < a.split("\\.").length) {
                name = a.replaceFirst(i(a), "").replaceFirst(".", "");
            }
            int sl = 0;
            if(0 < a.length()){
                String cmt = "";
                for(int i : comments.keySet()){
                    CommentData cd = comments.get(i);
                    if(cd.getInline() && cd.getContentLine() == sl){
                        cmt = " // " + cd.getContent();
                        break;
                    }
                }
                List<String> e = new ArrayList<>();
                if(scdata.containsKey(a)){
                    e = scdata.get(a);
                }
                e.add(bs + name + " > " + cmt);
                scdata.put(a, e);
                bs += 1;
            }
            for(String b : result.keySet()){
                Object o = result.get(b);
                String dtsc = i(a);
                String dtn = b.replaceFirst(dtsc, "");
                if(dtsc.equals(a)){
                    List<String> e = new ArrayList<>();
                    if(scdata.containsKey(a)){
                        e = scdata.get(a);
                    }
                    String cmt = "";
                    for(int i : comments.keySet()){
                        CommentData cd = comments.get(i);
                        if(cd.getInline() && cd.getContentLine() == sl){
                            cmt = " // " + cd.getContent();
                            break;
                        }
                    }
                    if(o == null){
                        e.add(bs + dtn + " > " + cmt);
                    } else if(o instanceof Boolean){
                        if(((Boolean) o ).equals(Boolean.TRUE)){
                            e.add(bs + dtn + " > true" + cmt);
                        } else {
                            e.add(bs + dtn + " > false" + cmt);
                        }
                    } else if(o instanceof List){
                        e.add(bs + dtn + " > " + cmt);
                        ArrayList<String> al = (ArrayList) o;
                        for(String v : al){
                            e.add(bs + "- " + v);
                        }
                    } else {
                        e.add(bs + dtn + " > " + o.toString() + cmt);
                    }
                    scdata.put(a, e);
                }
                sl++;
            }
        }
        // scdata to string
        List<String> r = new ArrayList<>();
        for(String j : scdata.keySet()){
            r.addAll(scdata.get(j));
        }
        return r;
    }
}
