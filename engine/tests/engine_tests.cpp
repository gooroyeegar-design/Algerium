#include "algerium/engine.h"
#include <cassert>
#include <iostream>
int main(){
 using namespace algerium;
 auto p=load_document(R"(<!doctype html><html><head><style>p{display:block;color:red;font-size:20px}</style></head><body><h1 id="x">Hello</h1><p class="a">World</p></body></html>)","https://example.com",800);
 assert(p.document.root);
 auto hs=query_selector_all(p.document.root.get(),"h1");assert(hs.size()==1&&hs[0]->attr("id")=="x");
 auto ps=query_selector_all(p.document.root.get(),"p");assert(ps.size()==1&&ps[0]->attr("class")=="a");
 assert(!p.boxes.empty());
 std::cout<<"Algerium engine smoke tests passed\n";
}
