#include "algerium/css.h"
#include <sstream>
#include <algorithm>
#include <cctype>
namespace algerium {
static std::string trim(std::string s){auto a=s.find_first_not_of(" \t\r\n");auto b=s.find_last_not_of(" \t\r\n");return a==std::string::npos?"":s.substr(a,b-a+1);}
static float px(const std::string& s,float fallback=0){try{if(s.size()>2&&s.substr(s.size()-2)=="px")return std::stof(s);return std::stof(s);}catch(...){return fallback;}}
static int spec(const std::string& s){int n=0;for(char c:s){if(c=='#')n+=100;else if(c=='.'||c=='['||c==':')n+=10;}if(!s.empty()&&s[0]!='#'&&s[0]!='.'&&s[0]!='['&&s[0]!=':')n+=1;return n;}
std::vector<CssRule> parse_css(const std::string& css){
 std::vector<CssRule> out;size_t p=0;
 while((p=css.find('{',p))!=std::string::npos){auto e=css.find('}',p+1);if(e==std::string::npos)break;CssRule r;r.selector=trim(css.substr(css.rfind('}',p)==std::string::npos?0:css.rfind('}',p)+1,p-(css.rfind('}',p)==std::string::npos?0:css.rfind('}',p)+1)));r.specificity=spec(r.selector);auto body=css.substr(p+1,e-p-1);std::stringstream ss(body);std::string d;while(std::getline(ss,d,';')){auto c=d.find(':');if(c!=std::string::npos)r.declarations.push_back({trim(d.substr(0,c)),trim(d.substr(c+1))});}if(!r.selector.empty())out.push_back(r);p=e+1;}
 return out;
}
ComputedStyle compute_style(const std::vector<CssRule>& rules,const std::string& tag,const std::unordered_map<std::string,std::string>& attrs,const ComputedStyle* parent){
 ComputedStyle s;if(parent){s.color=parent->color;s.font_family=parent->font_family;s.font_size=parent->font_size;}
 struct Pick{int score=-1;std::string v;};std::unordered_map<std::string,Pick> picks;
 auto matches=[&](const std::string& q){
 if(q=="*"||q==tag)return true;
 auto id=attrs.find("id");if(id!=attrs.end()&&q=="#"+id->second)return true;
 auto cl=attrs.find("class");
 if(cl!=attrs.end()){
   std::string wanted=q.size()&&q[0]=='.'?q.substr(1):"";
   if(!wanted.empty()){std::string classes=cl->second;size_t p=0;while(p<classes.size()){while(p<classes.size()&&std::isspace((unsigned char)classes[p]))p++;size_t e=p;while(e<classes.size()&&!std::isspace((unsigned char)classes[e]))e++;if(classes.substr(p,e-p)==wanted)return true;p=e;}}
 }
 return false;
};
 for(auto&r:rules)if(matches(r.selector))for(auto&d:r.declarations)if(r.specificity>=picks[d.property].score)picks[d.property]={r.specificity,d.value};
 auto inlineStyle=attrs.find("style");
 if(inlineStyle!=attrs.end()){
   auto inlineRules=parse_css("*{"+inlineStyle->second+"}");
   if(!inlineRules.empty()) for(const auto& d:inlineRules.front().declarations)picks[d.property]={1000,d.value};
 }
 for(auto&[k,p]:picks){auto v=p.v;if(k=="display")s.display=v;else if(k=="position")s.position=v;else if(k=="color")s.color=v;else if(k=="background"||k=="background-color")s.background=v;else if(k=="font-size")s.font_size=px(v,16);else if(k=="font-weight")s.bold=(v=="bold"||v=="700");else if(k=="width")s.width=px(v,-1);else if(k=="height")s.height=px(v,-1);else if(k=="margin")s.margin_top=s.margin_right=s.margin_bottom=s.margin_left=px(v);else if(k=="padding")s.padding_top=s.padding_right=s.padding_bottom=s.padding_left=px(v);}
 return s;
}
}
