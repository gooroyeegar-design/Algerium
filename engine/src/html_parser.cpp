#include "algerium/engine.h"
#include <cctype>
#include <algorithm>
#include <cstring>
#include <unordered_set>
namespace algerium {
static std::string lower(std::string s){for(char& c:s)c=(char)std::tolower((unsigned char)c);return s;}
static std::string trim(std::string s){auto a=s.find_first_not_of(" \t\r\n");auto b=s.find_last_not_of(" \t\r\n");return a==std::string::npos?"":s.substr(a,b-a+1);}
static std::string decode_entities(std::string s){
 const std::pair<const char*,const char*> e[]={{"&nbsp;"," "},{"&amp;","&"},{"&lt;","<"},{"&gt;",">"},{"&quot;","\""},{"&#39;","'"}};
 for(auto &x:e){size_t p=0;while((p=s.find(x.first,p))!=std::string::npos){s.replace(p,strlen(x.first),x.second);p+=strlen(x.second);}}
 return s;
}
static std::unordered_map<std::string,std::string> parse_attrs(const std::string& raw){
 std::unordered_map<std::string,std::string> a;size_t p=0;
 while(p<raw.size()){while(p<raw.size()&&std::isspace((unsigned char)raw[p]))p++;
  size_t k=p;while(p<raw.size()&&!std::isspace((unsigned char)raw[p])&&raw[p]!='='&&raw[p]!='/'&&raw[p]!='>')p++;
  if(k==p){p++;continue;} std::string key=lower(raw.substr(k,p-k));
  while(p<raw.size()&&std::isspace((unsigned char)raw[p]))p++;
  if(p<raw.size()&&raw[p]=='='){p++;while(p<raw.size()&&std::isspace((unsigned char)raw[p]))p++;
   std::string v;if(p<raw.size()&&(raw[p]=='"'||raw[p]=='\'')){char q=raw[p++];size_t v0=p;while(p<raw.size()&&raw[p]!=q)p++;v=raw.substr(v0,p-v0);if(p<raw.size())p++;}
   else{size_t v0=p;while(p<raw.size()&&!std::isspace((unsigned char)raw[p])&&raw[p]!='>')p++;v=raw.substr(v0,p-v0);}
   a[key]=decode_entities(v);
  }else a[key]="";
 } return a;
}
Document parse_html(const std::string& html,const std::string& url){
 Document d;d.url=url;std::vector<Node*> stack{d.root.get()};size_t p=0;
 static const std::unordered_set<std::string> voids={"area","base","br","col","embed","hr","img","input","link","meta","param","source","track","wbr"};
 while(p<html.size()){
  if(html[p]!='<'){size_t e=html.find('<',p);if(e==std::string::npos)e=html.size();std::string t=decode_entities(html.substr(p,e-p));if(!t.empty()&&!trim(t).empty())stack.back()->append(std::make_unique<Node>(Node::Type::Text,"#text"))->text=t;p=e;continue;}
  if(html.compare(p,4,"<!--")==0){size_t e=html.find("-->",p+4);p=e==std::string::npos?html.size():e+3;continue;}
  size_t e=p+1;bool quote=false;char q=0;for(;e<html.size();++e){char c=html[e];if((c=='"'||c=='\'')&&!quote){quote=true;q=c;}else if(quote&&c==q)quote=false;else if(c=='>'&&!quote)break;}if(e>=html.size())break;
  std::string inside=trim(html.substr(p+1,e-p-1));p=e+1;if(inside.empty())continue;
  if(inside[0]=='!')continue;
  if(inside[0]=='/'){std::string close=lower(trim(inside.substr(1)));size_t sp=close.find_first_of(" \t\r\n");if(sp!=std::string::npos)close.resize(sp);
    for(size_t i=stack.size();i>1;--i)if(stack[i-1]->name==close){stack.resize(i-1);break;}continue;}
  if(inside[0]=='?')continue;
  bool self=inside.back()=='/';if(self)inside.pop_back();
  size_t sp=inside.find_first_of(" \t\r\n");std::string name=lower(sp==std::string::npos?inside:inside.substr(0,sp));
  auto n=std::make_unique<Node>(Node::Type::Element,name);if(sp!=std::string::npos)n->attrs=parse_attrs(inside.substr(sp+1));Node* raw=stack.back()->append(std::move(n));
  if(name=="title"){std::string title;for(auto&c:raw->children)if(c->type==Node::Type::Text)title+=c->text;d.title=title;}
  if(!self&&!voids.count(name)){stack.push_back(raw);if(name=="script"||name=="style"||name=="textarea"){std::string endtag="</"+name;size_t z=html.find(endtag,p);if(z!=std::string::npos){std::string body=html.substr(p,z-p);if(!body.empty())raw->append(std::make_unique<Node>(Node::Type::Text,"#text"))->text=body;size_t ze=html.find('>',z);p=ze==std::string::npos?html.size():ze+1;stack.pop_back();}}}
 }
 for(Node* n: descendants(d.root.get())){ if(n->name=="title"){ std::string t; for(auto& ch:n->children) if(ch->type==Node::Type::Text) t+=ch->text; d.title=decode_entities(t); break; } }\n return d;\n}\n}