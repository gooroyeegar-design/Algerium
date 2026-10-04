#pragma once
#include "layout.h"
#include <cstdint>
#include <string>
#include <vector>
namespace algerium {
struct PaintCommand { enum class Type { Rect, Text, Border }; Type type; float x=0,y=0,w=0,h=0; std::string text; std::string color; float font_size=16; };
std::vector<PaintCommand> paint_document(const Document& doc,const std::vector<LayoutBox>& boxes);
}
