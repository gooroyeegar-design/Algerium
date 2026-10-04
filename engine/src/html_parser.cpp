#include "algerium/engine.h"
#include <cctype>
#include <algorithm>
namespace algerium {
static std::string lower(std::string s){for(char&c:s)c=(char)std::tolower((unsigned char)c);return s;}
static std::string trim(std::string s){auto a=s.find_first_not_of(" \t\r\n");auto b=s.find_last_not_of(" \t\r\n");return a==std::string::npos?"":s.substr(a,b-a+1);}
static std::unordered_map<std::string,std::string> attrs(const std::string& raw){
 std::unordered_map<std::string,std::string> a;size_t p=0;while(p<raw.size()){while(p<raw.size()&&std::isspace((unsigned char)raw[p]))p++;size_t k=p;while(p<raw.size()&&!std::isspace((unsigned char)raw[p])&&raw[p]!='='&&raw[p]!='>')p++;if(k==p){p++;continue;}auto key=lower(raw.substr(k,p-k));while(p<raw.size()&&std::isspace((unsigned char)raw[p]))p++;if(p<raw.size()&&raw[p]=='='){p++;while(p<raw.size()&&std::isspace((unsigned char)raw[p]))p++;char q=0;if(p<raw.size()&&(raw[p]=='"'||raw[p]=='\'')){q=raw[p++];size_t v=p;while(p<raw.size()&&raw[p]!=q)p++;a[key]=raw.substr(v,p-v);if(p<raw.size())p++;}else{size_t v=p;while(p<raw.size()&&!std::isspace((unsigned char)raw[p])&&raw[p]!='>')p++;a[key]=raw.substr(v,p-v);}}}return a;}
Document parse_html(const std::string& html,const std::string& url){
 Document d;d.url=url;std::vector<Node*> stack{d.root.get()};size_t p=0;
 while(p<html.size()){if(html[p]=='<'){auto e=html.find('>',p+1);if(e==std::string::npos)break;auto inside=trim(html.substr(p+1,e-p-1));if(inside.rfind("!--",0)==0){auto ce=html.find("-->",p+4);p=ce==std::string::npos?html.size():ce+3;continue;}if(!inside.empty()&&inside[0]=='/'){if(stack.size()>1)stack.pop_back();p=e+1;continue;}bool self=!inside.empty()&&inside.back()=='/';if(self)inside.pop_back();auto sp=inside.find_first_of(" \t\r\n");std::string name=lower(sp==std::string::npos?inside:inside.substr(0,sp));auto n=std::make_unique<Node>(Node::Type::Element,name);if(sp!=std::string::npos)n->attrs=attrs(inside.substr(sp+1));Node* raw=stack.back()->append(std::move(n));if(name=="title")d.title.clear();if(!self&&name!="meta"&&name!="link"&&name!="img"&&name!="input"&&name!="br"&&name!="hr"&&name!="source")stack.push_back(raw);p=e+1;}else{auto e=html.find('<',p);auto t=html.substr(p,e==std::string::npos?html.size()-p:e-p);if(!trim(t).empty())stack.back()->append(std::make_unique<Node>(Node::Type::Text,"#text"))->text=t;p=e==std::string::npos?html.size():e;}}
 return d;
}
}
