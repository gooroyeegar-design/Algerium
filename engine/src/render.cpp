#include "algerium/render.h"
#include <algorithm>
#include <cctype>
namespace algerium {
static std::string text_content(const Node*n){
 std::string s;if(!n)return s;
 if(n->type==Node::Type::Text)return n->text;
 for(const auto&c:n->children)s+=text_content(c.get());
 return s;
}
static std::string tag_color(const ComputedStyle&s){return s.color.empty()?"#000000":s.color;}
std::vector<PaintCommand> paint_document(const Document&,const std::vector<LayoutBox>&boxes){
 std::vector<PaintCommand>out;
 for(const auto&b:boxes){
  if(!b.node||b.node->type!=Node::Type::Element||b.style.display=="none")continue;
  if(b.style.background!="transparent")out.push_back({PaintCommand::Type::Rect,b.x,b.y,b.width,b.height,{},b.style.background,b.style.font_size});
  if(b.style.border_width>0)out.push_back({PaintCommand::Type::Border,b.x,b.y,b.width,b.height,{},tag_color(b.style),b.style.border_width});
  auto t=text_content(b.node);if(t.empty())continue;
  float tx=b.x+b.style.padding_left;
  if(b.style.text_align=="center")tx=b.x+std::max(0.f,(b.width-b.style.padding_left-b.style.padding_right)*.5f);
  else if(b.style.text_align=="right")tx=b.x+std::max(0.f,b.width-b.style.padding_right);
  out.push_back({PaintCommand::Type::Text,tx,b.y+b.style.padding_top,b.width,b.height,t,tag_color(b.style),b.style.font_size});
 }
 return out;
}
}