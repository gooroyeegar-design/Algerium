#include "algerium/engine.h"
#include "algerium/render.h"
#include <algorithm>
#include <cassert>
#include <iostream>
int main(){
 using namespace algerium;
 auto p=load_document(R"(<!doctype html><html><head><style>
 #app{display:block;padding:10px} #app .row{display:flex;gap:8px} .row a{color:red;font-size:20px} [data-x="1"]{font-weight:700}
 </style></head><body><div id="app"><div class="row"><a href="/x" data-x="1">Hello &amp; world</a><span>!</span></div><p>wrapped text</p></div></body></html>)","https://example.com",800);
 assert(p.document.root);
 auto hs=query_selector_all(p.document.root.get(),"a");assert(hs.size()==1&&hs[0]->attr("href")=="/x");
 auto nested=query_selector_all(p.document.root.get(),"#app .row a");assert(nested.size()==1);
 auto attrs=query_selector_all(p.document.root.get(),"[data-x=\"1\"]");assert(attrs.size()==1);
 assert(hs[0]->children.size()==1&&hs[0]->children[0]->text=="Hello & world");
 assert(p.boxes.size()>=5);
 auto paint=paint_document(p.document,p.boxes);assert(!paint.empty());
 auto duplicateLabels=std::count_if(paint.begin(),paint.end(),[](const PaintCommand&cmd){
  return cmd.type==PaintCommand::Type::Text&&cmd.text=="Hello & world";
 });
 assert(duplicateLabels==1);
 std::cout<<"Algerium engine compatibility smoke tests passed\n";
}