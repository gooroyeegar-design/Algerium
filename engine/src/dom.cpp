#include "algerium/dom.h"
#include <algorithm>
#include <cctype>
namespace algerium {
static bool match(Node* n,std::string s){
 if(!n||n->type!=Node::Type::Element)return false;
 if(s.empty())return false;
 auto hash=s.find('#'), dot=s.find('.');
 std::string tag=s.substr(0,std::min({hash==std::string::npos?s.size():hash,dot==std::string::npos?s.size():dot}));
 if(!tag.empty()&&tag!="*"&&n->name!=tag)return false;
 if(hash!=std::string::npos){auto end=dot==std::string::npos?s.size():dot;if(n->attr("id")!=s.substr(hash+1,end-hash-1))return false;}
 if(dot!=std::string::npos){auto cls=n->attr("class"),want=s.substr(dot+1);if(cls.find(want)==std::string::npos)return false;}
 return true;
}
std::vector<Node*> descendants(Node* root){std::vector<Node*> r;if(!root)return r;for(auto& c:root->children){r.push_back(c.get());auto q=descendants(c.get());r.insert(r.end(),q.begin(),q.end());}return r;}
std::vector<Node*> query_selector_all(Node* root,const std::string& selector){
 std::vector<Node*> out; auto all=descendants(root);
 for(auto* n:all) if(match(n,selector)) out.push_back(n);
 return out;
}
}
