#include "algerium/layout.h"
#include <algorithm>
#include <functional>
#include <string>
namespace algerium {
static bool blockish(const std::string& n,const ComputedStyle&s){
 return s.display=="block"||s.display=="flex"||n=="html"||n=="body"||n=="div"||n=="p"||n=="section"||n=="article"||n=="header"||n=="footer"||n=="main"||n=="nav"||n=="h1"||n=="h2"||n=="h3"||n=="ul"||n=="ol"||n=="li"||n=="form";
}
static float intrinsic_text_height(const Node*n,const ComputedStyle&s,float width){
 std::string t; if(!n)return s.font_size*s.line_height;
 std::function<void(const Node*)> collect=[&](const Node*x){if(x->type==Node::Type::Text)t+=x->text;else for(auto&c:x->children)collect(c.get());};collect(n);
 size_t chars=std::max<size_t>(1,t.size());float chars_per_line=std::max(8.f,width/std::max(6.f,s.font_size*.52f));
 return std::max(s.font_size*s.line_height,float((chars+size_t(chars_per_line)-1)/size_t(chars_per_line))*s.font_size*s.line_height);
}
std::vector<LayoutBox> layout_document(const Document& doc,const std::vector<CssRule>& rules,float viewport_width){
 std::vector<LayoutBox> out;
 std::function<float(const Node*,float,float,const ComputedStyle*)> layout=[&](const Node*n,float x,float y,const ComputedStyle*parent)->float{
  if(!n||n->type!=Node::Type::Element)return 0;
  ComputedStyle s=compute_style(rules,n->name,n->attrs,parent);if(s.display=="none")return 0;
  float outerW=s.width>0?s.width:std::max(0.f,viewport_width-x-8);
  float contentW=std::max(1.f,outerW-s.padding_left-s.padding_right);
  float h=s.height>0?s.height:0;
  if(n->name=="h1")h=std::max(h,42.f);else if(n->name=="h2")h=std::max(h,34.f);else if(n->name=="p")h=std::max(h,28.f);
  if(h<=0)h=intrinsic_text_height(n,s,contentW)+s.padding_top+s.padding_bottom;
  size_t index=out.size();out.push_back({n,s,x+s.margin_left,y+s.margin_top,outerW,h});
  float childY=y+s.margin_top+s.padding_top; float childX=x+s.margin_left+s.padding_left;
  if(s.display=="flex"){
   bool row=s.flex_direction!="column";float cursor=row?childX:childY;
   for(auto&ch:n->children){if(ch->type!=Node::Type::Element)continue;
    float used=layout(ch.get(),row?cursor:childX,row?childY:cursor,&s);
    if(row){cursor+=out.back().width+s.gap;}else{cursor+=used+s.gap;}
   }
  }else{
   for(auto&ch:n->children){if(ch->type!=Node::Type::Element)continue;float used=layout(ch.get(),childX,childY,&s);childY+=used;}
  }
  float totalH=std::max(h,childY-(y+s.margin_top)+s.padding_bottom);
  out[index].height=totalH;
  return totalH+s.margin_top+s.margin_bottom;
 };
 layout(doc.root.get(),8,8,nullptr);return out;
}
}