#include "algerium/render.h"
#include <algorithm>
namespace algerium {
static std::string direct_text(const Node*n){
 if(!n||n->type!=Node::Type::Element)return {};
 std::string s;
 for(const auto&c:n->children)if(c&&c->type==Node::Type::Text)s+=c->text;
 return s;
}
static std::string all_text(const Node*n){if(!n)return {};if(n->type==Node::Type::Text)return n->text;std::string s;for(const auto&c:n->children)s+=all_text(c.get());return s;}
static bool has_child_box(const Node*n,const std::vector<LayoutBox>&boxes){if(!n)return false;for(const auto&c:n->children){if(!c)continue;if(c->type==Node::Type::Element){for(const auto&b:boxes)if(b.node==c.get())return true;if(has_child_box(c.get(),boxes))return true;}}return false;}
static std::string tag_color(const ComputedStyle&s){return s.color.empty()?"#000000":s.color;}
std::vector<PaintCommand> paint_document(const Document&,const std::vector<LayoutBox>&boxes){
 std::vector<PaintCommand>out;
 for(const auto&b:boxes){if(!b.node||b.node->type!=Node::Type::Element||b.style.display=="none")continue;
  if(b.style.background!="transparent")out.push_back({PaintCommand::Type::Rect,b.x,b.y,b.width,b.height,{},b.style.background,b.style.font_size});
  if(b.style.border_width>0)out.push_back({PaintCommand::Type::Border,b.x,b.y,b.width,b.height,{},tag_color(b.style),b.style.border_width});
  std::string t=has_child_box(b.node,boxes)?direct_text(b.node):all_text(b.node);if(t.empty())continue;
  float tx=b.x+b.style.padding_left;float estimated=t.size()*b.style.font_size*.52f;
  if(b.style.text_align=="center")tx=b.x+std::max(0.f,(b.width-estimated)*.5f);else if(b.style.text_align=="right")tx=b.x+std::max(0.f,b.width-estimated-b.style.padding_right);
  out.push_back({PaintCommand::Type::Text,tx,b.y+b.style.padding_top,b.width,b.height,t,tag_color(b.style),b.style.font_size});}
 return out;
}
}
