#include "algerium/engine.h"
#include <regex>
namespace algerium {
Page load_document(const std::string& html,const std::string& url,float viewport_width){
 Page p;p.document=parse_html(html,url);
 std::regex style_re(R"(<style[^>]*>([\s\S]*?)</style>)",std::regex::icase);std::smatch m;std::string src=html;while(std::regex_search(src,m,style_re)){auto r=parse_css(m[1].str());p.rules.insert(p.rules.end(),r.begin(),r.end());src=m.suffix().str();}
 p.boxes=layout_document(p.document,p.rules,viewport_width);return p;
}
}
