#include "algerium/render.h"
#include <algorithm>
namespace algerium {
static std::string text_of(const Node* n){std::string s;if(!n)return s;if(n->type==Node::Type::Text)return n->text;for(const auto&c:n->children)s+=text_of(c.get());return s;}
std::vector<PaintCommand> paint_document(const Document&,const std::vector<LayoutBox>& boxes){
 std::vector<PaintCommand> out;
 for(const auto&b:boxes){
  if(!b.node||b.node->type!=Node::Type::Element)continue;
  if(b.style.display=="none")continue;
  if(b.style.background!="transparent")out.push_back({PaintCommand::Type::Rect,b.x,b.y,b.width,b.height,{},b.style.background,b.style.font_size});
  auto text=text_of(b.node);if(!text.empty())out.push_back({PaintCommand::Type::Text,b.x+ b.style.padding_left,b.y+b.style.padding_top,b.width,b.height,text,b.style.color,b.style.font_size});
 }
 return out;
}
}
