#include "algerium/engine.h"
#include <cstddef>
#include <cstdint>
extern "C" int LLVMFuzzerTestOneInput(const uint8_t*data,size_t size){
 std::string html(reinterpret_cast<const char*>(data),size);
 if(html.size()>262144) html.resize(262144);
 auto doc=algerium::parse_html(html,"https://fuzz.example/");
 return doc.root?0:0;
}
