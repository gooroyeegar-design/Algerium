#include "algerium/css.h"
#include <algorithm>
#include <cctype>
#include <sstream>
namespace algerium {
static std::string trim(std::string s){auto a=s.find_first_not_of(" \t\r\n");auto b=s.find_last_not_of(" \t\r\n");return a==std::string::npos?"":s.substr(a,b-a+1);}
static std::string lower(std::string s){for(char&c:s)c=(char)std::tolower((unsigned char)c);return s;}
static float px(const std::string&v,float f=0){try{if(v=="auto")return f;if(v.size()>2&&v.substr(v.size()-2)=="px")return std::stof(v);return std::stof(v);}catch(...){return f;}}
static void split4(const std::string&v,float&a,float&b,float&c,float&d){std::stringstream ss(v);std::string x;std::vector<float> q;while(ss>>x){if(x.back()==';')x.pop_back();q.push_back(px(x));}if(q.empty())return;if(q.size()==1)a=b=c=d=q[0];else if(q.size()==2){a=c=q[0];b=d=q[1];}else if(q.size()==3){a=q[0];b=d=q[1];c=q[2];}else{a=q[0];b=q[1];c=q[2];d=q[3];}}
static int spec(const std::string&s){int n=0;for(char c:s){if(c=='#')n+=100;else if(c=='.'||c=='['||c==':')n+=10;}for(size_t i=0;i<s.size();++i)if((std::isalpha((unsigned char)s[i])&&i==0)||(i&&s[i-1]==' '&&std::isalpha((unsigned char)s[i])))n++;return n;}
std::vector<CssRule> parse_css(const std::string&css){
 std::vector<CssRule>out;size_t p=0;
 while((p=css.find('{',p))!=std::string::npos){size_t e=css.find('}',p+1);if(e==std::string::npos)break;
  size_t b=css.rfind('}',p);std::string sel=trim(css.substr(b==std::string::npos?0:b+1,p-(b==std::string::npos?0:b+1)));
  auto body=css.substr(p+1,e-p-1);std::stringstream ss(body);std::string d;CssRule r;r.selector=sel;r.specificity=spec(sel);
  while(std::getline(ss,d,';')){auto c=d.find(':');if(c==std::string::npos)continue;auto k=trim(d.substr(0,c));auto v=trim(d.substr(c+1));if(!k.empty())r.declarations.push_back({lower(k),v});}
  if(!r.selector.empty())out.push_back(r);p=e+1;
 } return out;
}
static bool simple_match(const std::string&q,const std::string&tag,const std::unordered_map<std::string,std::string>&a){
 size_t p=0;while(p<q.size()){
  if(q[p]=='*'){++p;continue;}
  if(q[p]=='#'||q[p]=='.'){char k=q[p++];size_t e=p;while(e<q.size()&&q[e]!='.'&&q[e]!='#'&&q[e]!='[')++e;auto v=q.substr(p,e-p);
   if(k=='#'&&a.find("id")==a.end()||k=='#'&&a.at("id")!=v)return false;
   if(k=='.'){auto it=a.find("class");if(it==a.end()||it->second.find(v)==std::string::npos)return false;}p=e;continue;
  }
  if(q[p]=='['){auto e=q.find(']',p);if(e==std::string::npos)return false;auto x=q.substr(p+1,e-p-1);auto eq=x.find('=');auto k=eq==std::string::npos?x:x.substr(0,eq);auto it=a.find(k);if(it==a.end())return false;if(eq!=std::string::npos){auto v=x.substr(eq+1);if(v.size()>=2 && (v.front()=='"' || v.front()=='\\'')){char z=v.front();if(v.back()==z)v=v.substr(1,v.size()-2);}if(it->second!=v)return false;}p=e+1;continue;}
  auto e=p;while(e<q.size()&&q[e]!='.'&&q[e]!='#'&&q[e]!='[')++e;auto t=q.substr(p,e-p);if(t!="*"&&t!=tag)return false;p=e;
 }return true;
}
static bool matches(const std::string&q,const std::string&tag,const std::unordered_map<std::string,std::string>&a){
 std::stringstream ss(q);std::string part;std::vector<std::string>v;while(ss>>part)v.push_back(part);if(v.empty())return false;return simple_match(v.back(),tag,a);
}
static bool is_block_tag(const std::string&t){return t=="html"||t=="body"||t=="div"||t=="main"||t=="section"||t=="article"||t=="header"||t=="footer"||t=="nav"||t=="aside"||t=="p"||t=="h1"||t=="h2"||t=="h3"||t=="h4"||t=="h5"||t=="h6"||t=="ul"||t=="ol"||t=="li"||t=="form"||t=="table"||t=="tr"||t=="pre";}\nComputedStyle compute_style(const std::vector<CssRule>&rules,const std::string&tag,const std::unordered_map<std::string,std::string>&attrs,const ComputedStyle*parent){
 ComputedStyle s;\n if(is_block_tag(tag)) s.display="block";if(parent){s.color=parent->color;s.font_family=parent->font_family;s.font_size=parent->font_size;s.line_height=parent->line_height;}
 struct Pick{int score=-1;size_t order=0;std::string v;};std::unordered_map<std::string,Pick>picks;size_t order=0;
 for(auto&r:rules){if(matches(r.selector,tag,attrs))for(auto&d:r.declarations){auto&p=picks[d.property];int score=r.specificity;if(score>p.score||(score==p.score&&order>=p.order))p={score,order,d.value};}++order;}
 auto in=attrs.find("style");if(in!=attrs.end()){auto rs=parse_css("*{"+in->second+"}");if(!rs.empty())for(auto&d:rs[0].declarations)picks[d.property]={1000,order,d.value};}
 for(auto&[k,p]:picks){auto v=p.v;
  if(k=="display")s.display=lower(v);else if(k=="position")s.position=lower(v);else if(k=="color")s.color=v;else if(k=="background"||k=="background-color")s.background=v;
  else if(k=="font-size")s.font_size=px(v,16);else if(k=="font-weight")s.bold=(v=="bold"||v=="700"||v=="600");
  else if(k=="width")s.width=px(v,-1);else if(k=="height")s.height=px(v,-1);
  else if(k=="margin")split4(v,s.margin_top,s.margin_right,s.margin_bottom,s.margin_left);
  else if(k=="padding")split4(v,s.padding_top,s.padding_right,s.padding_bottom,s.padding_left);
  else if(k=="line-height")s.line_height=(v.find("px")!=std::string::npos)?px(v,19.2f):px(v,1.2f);
  else if(k=="gap")s.gap=px(v);else if(k=="row-gap")s.gap=px(v);else if(k=="flex-direction")s.flex_direction=lower(v);\n  else if(k=="justify-content")s.justify_content=lower(v);else if(k=="align-items")s.align_items=lower(v);else if(k=="overflow")s.overflow=lower(v);\n  else if(k=="min-width")s.min_width=px(v);else if(k=="max-width")s.max_width=px(v,-1);else if(k=="min-height")s.min_height=px(v);else if(k=="max-height")s.max_height=px(v,-1);\n  else if(k=="flex-grow")s.flex_grow=px(v);else if(k=="flex-shrink")s.flex_shrink=px(v,1);else if(k=="flex-basis")s.flex_basis=px(v,-1);\n  else if(k=="opacity")s.opacity=px(v,1);else if(k=="letter-spacing")s.letter_spacing=px(v);else if(k=="word-spacing")s.word_spacing=px(v);
  else if(k=="text-align")s.text_align=lower(v);else if(k=="border-width")s.border_width=px(v);else if(k=="border-radius")s.border_radius=px(v);
 }return s;
}
}