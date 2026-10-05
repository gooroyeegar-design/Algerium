#include "algerium/layout.h"
#include <algorithm>
#include <functional>
#include <string>
#include <cctype>
namespace algerium {
static bool blockish(const std::string&n,const ComputedStyle&s){return s.display=="block"||s.display=="flex"||s.display=="table"||n=="html"||n=="body"||n=="div"||n=="p"||n=="section"||n=="article"||n=="header"||n=="footer"||n=="main"||n=="nav"||n=="aside"||n=="h1"||n=="h2"||n=="h3"||n=="h4"||n=="ul"||n=="ol"||n=="li"||n=="form";}
static std::string text_content(const Node*n){std::string t;if(!n)return t;if(n->type==Node::Type::Text)return n->text;for(auto&c:n->children)t+=text_content(c.get());return t;}
static float text_height(const Node*n,const ComputedStyle&s,float w){std::string t=text_content(n);size_t chars=std::max<size_t>(1,t.size());float cw=std::max(4.f,s.font_size*.52f);size_t per=std::max<size_t>(1,(size_t)(w/cw));size_t lines=1,col=0;for(char c:t){if(c=='\n'){++lines;col=0;}else if(++col>per){++lines;col=1;}}return lines*s.font_size*s.line_height;}
std::vector<LayoutBox> layout_document(const Document&doc,const std::vector<CssRule>&rules,float viewport_width){
 std::vector<LayoutBox>out;
 std::function<float(const Node*,float,float,float,const ComputedStyle*)>lay=
 [&](const Node*n,float x,float y,float available,const ComputedStyle*parent)->float{
  if(!n||n->type!=Node::Type::Element)return 0;ComputedStyle s=compute_style(rules,n->name,n->attrs,parent);if(s.display=="none")return 0;
  float marginW=s.margin_left+s.margin_right,padW=s.padding_left+s.padding_right,w=s.width>0?s.width:std::max(0.f,available-marginW);float contentW=std::max(1.f,w-padW);\n  if(s.min_width>0) w=std::max(w,s.min_width); if(s.max_width>=0) w=std::min(w,s.max_width);\n  if(s.min_height>0) h=std::max(h,s.min_height); if(s.max_height>=0) h=std::min(h,s.max_height);
  float contentX=x+s.margin_left+s.padding_left,contentY=y+s.margin_top+s.padding_top,h=s.height>0?s.height:0;
  if(h<=0&&(n->name=="img"||n->name=="video"||n->name=="canvas"))h=150;
  if(h<=0&&!blockish(n->name,s))h=text_height(n,s,contentW)+s.padding_top+s.padding_bottom;
  if(h<=0)h=(n->name=="h1"?42:n->name=="h2"?34:n->name=="h3"?28:n->name=="p"?28:20)+s.padding_top+s.padding_bottom;
  size_t idx=out.size();out.push_back({n,s,x+s.margin_left,y+s.margin_top,w,h});
  float cursorY=contentY,maxBottom=contentY;
  std::vector<const Node*>children;for(auto&c:n->children)if(c->type==Node::Type::Element)children.push_back(c.get());
  if(s.display=="flex"){
   bool row=s.flex_direction!="column";float gap=s.gap;
   if(row){
    float free=std::max(0.f,contentW-gap*std::max(0,(int)children.size()-1)); float grow=0; for(auto*c:children){auto cs=compute_style(rules,c->name,c->attrs,&s);grow+=cs.flex_grow;} float each=children.empty()?0:std::max(1.f,free/std::max<size_t>(1,children.size()));float cursorX=contentX;
    for(auto*c:children){size_t before=out.size();float used=lay(c,cursorX,contentY,each,&s);float childW=each;if(out.size()>before)childW=out[before].width;cursorX+=childW+gap;maxBottom=std::max(maxBottom,contentY+used);}
   }else{
    for(auto*c:children){float used=lay(c,contentX,cursorY,contentW,&s);cursorY+=used+gap;maxBottom=std::max(maxBottom,cursorY);}
   }
  }else if(blockish(n->name,s)){
   for(auto*c:children){float used=lay(c,contentX,cursorY,contentW,&s);cursorY+=used;maxBottom=std::max(maxBottom,cursorY);}
  }
  float childrenH=std::max(0.f,maxBottom-contentY)+s.padding_top+s.padding_bottom;out[idx].height=std::max(h,childrenH);return out[idx].height+s.margin_top+s.margin_bottom;
 };
 lay(doc.root.get(),0,0,viewport_width,nullptr);return out;
}
}