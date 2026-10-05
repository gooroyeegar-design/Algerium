#include "algerium/render.h"
#include <algorithm>
namespace algerium {
static std::string text_of(const Node*n){std::string s;if(!n)return s;if(n->type==Node::Type::Text)return n->text;for(const auto&c:n->children)s+=text_of(c.get());return s;}
std::vector<PaintCommand> paint_document(const Document&,const std::vector<LayoutBox>&boxes){
 std::vector<PaintCommand> out;
 for(const auto&b:boxes){
  if(!b.node||b.node->type!=Node::Type::Element||b.style.display=="none")continue;
  if(b.style.background!="transparent")out.push_back({PaintCommand::Type::Rect,b.x,b.y,b.width,b.height,{},b.style.background,b.style.font_size});
  if(b.style.border_width>0)out.push_back({PaintCommand::Type::Border,b.x,b.y,b.width,b.height,{},b.style.color,b.style.border_width});
  std::string text=text_of(b.node); if(!text.empty()){
   float tx=b.x+b.style.padding_left,ty=b.y+b.style.padding_top;
   if(b.style.text_align=="center")tx=b.x+b.width*.25f;
   else if(b.style.text_align=="right")tx=b.x+b.width*.5f;
   out.push_back({PaintCommand::Type::Text,tx,ty,b.width,b.height,text,b.style.color,b.style.font_size});
  }
 }
 return out;
}
}