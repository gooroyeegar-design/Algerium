static bool matches_simple(const Node*n,const std::string&s){if(!n||n->type!=Node::Type::Element)return false; std::string q=s; auto p=q.find("#"); if(p!=std::string::npos && n->attr("id")!=q.substr(p+1)) return false; p=q.find("."); if(p!=std::string::npos){auto cls=n->attr("class"); if(cls.find(q.substr(p+1))==std::string::npos)return false;} auto b=q.find("["); if(b!=std::string::npos){auto e=q.find("]",b); if(e!=std::string::npos && !n->has(q.substr(b+1,e-b-1)))return false;} if(q[0]!='#'&&q[0]!='.'&&q[0]!='['&&n->name!=q)return false; return true;}
#include "algerium/dom.h"
#include <algorithm>
#include <cctype>
#include <sstream>

namespace algerium {
static std::vector<std::string> parts(std::string s){
 std::vector<std::string> r; std::string cur; bool ws=false;
 for(char c:s){
  if(std::isspace((unsigned char)c)){ if(!cur.empty()){r.push_back(cur);cur.clear();} ws=true; }
  else { if(ws&&!r.empty()){} ws=false; cur+=c; }
 }
 if(!cur.empty())r.push_back(cur); return r;
}
static bool simple_match(Node* n,std::string s){
 if(!n||n->type!=Node::Type::Element||s.empty())return false;
 auto attr=[&](const std::string& k){return n->attr(k);};
 size_t pos=0;
 while(pos<s.size()){
  if(s[pos]=='*'){++pos;continue;}
  if(s[pos]=='#'||s[pos]=='.'){
   char kind=s[pos++];size_t e=pos;while(e<s.size()&&s[e]!='.'&&s[e]!='#'&&s[e]!='[')++e;
   std::string v=s.substr(pos,e-pos);
   if(kind=='#'&&attr("id")!=v)return false;
   if(kind=='.'){
    auto cs=parts(attr("class")); if(std::find(cs.begin(),cs.end(),v)==cs.end())return false;
   }
   pos=e;continue;
  }
  if(s[pos]=='['){
   size_t e=s.find(']',pos+1);if(e==std::string::npos)return false;
   std::string q=s.substr(pos+1,e-pos-1);auto eq=q.find('=');
   std::string k=eq==std::string::npos?q:q.substr(0,eq);
   if(!n->has(k))return false;
   if(eq!=std::string::npos){std::string v=q.substr(eq+1);if(v.size()>=2&&((v.front()=='"'&&v.back()=='"')||(v.front()=='\''&&v.back()=='\'')))v=v.substr(1,v.size()-2);if(attr(k)!=v)return false;}
   pos=e+1;continue;
  }
  size_t e=pos;while(e<s.size()&&s[e]!='.'&&s[e]!='#'&&s[e]!='[')++e;
  std::string tag=s.substr(pos,e-pos);if(tag!="*"&&tag!=n->name)return false;pos=e;
 }
 return true;
}
static bool matches_chain(Node* n,const std::vector<std::string>& chain){
 int i=(int)chain.size()-1;Node* cur=n;
 if(i<0||!simple_match(cur,chain[i]))return false;
 --i;cur=cur->parent;
 while(i>=0){
  while(cur&&!simple_match(cur,chain[i]))cur=cur->parent;
  if(!cur)return false;--i;cur=cur->parent;
 }
 return true;
}
std::vector<Node*> descendants(Node* root){std::vector<Node*> r;if(!root)return r;for(auto& c:root->children){r.push_back(c.get());auto q=descendants(c.get());r.insert(r.end(),q.begin(),q.end());}return r;}
std::vector<Node*> query_selector_all(Node* root,const std::string& selector){
 std::vector<Node*> out;auto chain=parts(selector);for(auto* n:descendants(root))if(matches_chain(n,chain))out.push_back(n);return out;
}
}