#include "algerium/layout.h"
#include <algorithm>
#include <functional>
namespace algerium {
static bool blockish(const std::string& n,const ComputedStyle&s){return s.display=="block"||n=="html"||n=="body"||n=="div"||n=="p"||n=="section"||n=="article"||n=="header"||n=="footer"||n=="main"||n=="nav"||n=="h1"||n=="h2"||n=="h3"||n=="ul"||n=="ol"||n=="li"||n=="form";}
std::vector<LayoutBox> layout_document(const Document& doc,const std::vector<CssRule>& rules,float viewport_width){
 std::vector<LayoutBox> out;float y=8;
 std::function<void(const Node*,float,const ComputedStyle*)> walk=[&](const Node*n,float x,const ComputedStyle*parent){if(n->type==Node::Type::Text)return;auto s=compute_style(rules,n->name,n->attrs,parent);float w=s.width>0?s.width:viewport_width-16;float h=s.height>0?s.height:(blockish(n->name,s)?24:0);if(n->name=="h1")h=42;if(n->name=="h2")h=34;if(n->name=="p")h=28;out.push_back({n,s,x,y,w,h});if(blockish(n->name,s))y+=h;for(auto&c:n->children)walk(c.get(),x,&out.back().style);};walk(doc.root.get(),8,nullptr);return out;
}
}
