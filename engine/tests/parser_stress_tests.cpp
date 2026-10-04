#include "algerium/engine.h"
#include <cassert>
#include <iostream>
#include <string>
int main(){
 const std::string cases[]={
   "<html><body><div><p>A<b>B</b>C</div><p>D",
   "<!doctype html><html><head><title>A &amp; B</title></head><body><img src=x><input disabled><br>ok</body></html>",
   "<style>p{color:red;font-size:20px}</style><p class=x style='font-weight:700'>Hello</p>",
   "<script>if(a < b){x='>'}</script><textarea>a < b</textarea>"
 };
 for(const auto&s:cases){auto d=algerium::parse_html(s,"https://example.test");assert(d.root);auto all=algerium::descendants(d.root.get());assert(all.size()<1000);}
 std::cout<<"parser stress tests passed\n";
}