#pragma once
#include "dom.h"
#include "css.h"
#include <vector>
namespace algerium {
struct LayoutBox { const Node* node=nullptr; ComputedStyle style; float x=0,y=0,width=0,height=0; };
std::vector<LayoutBox> layout_document(const Document& doc,float viewport_width);
}
