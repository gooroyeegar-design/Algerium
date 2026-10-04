#include "algerium/engine.h"
#include "algerium/render.h"
#include <cassert>
int main(){
 using namespace algerium;
 auto p=load_document("<html><body><div style='background:#fff'>Hello</div></body></html>","https://example.com",800);
 auto paint=paint_document(p.document,p.boxes);
 assert(!paint.empty());
}
