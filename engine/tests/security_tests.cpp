#include "algerium/security.h"
#include "algerium/events.h"
#include <cassert>
int main(){
 using namespace algerium;
 assert(same_origin(parse_origin("https://example.com/a"),parse_origin("https://example.com/b")));
 assert(!same_origin(parse_origin("https://example.com"),parse_origin("http://example.com")));
 assert(!same_origin(parse_origin("https://example.com"),parse_origin("https://evil.example")));
 assert(is_private_host("127.0.0.1")); assert(is_private_host("192.168.1.10")); assert(!is_private_host("8.8.8.8"));
 assert(is_safe_navigation("https://example.com")); assert(!is_safe_navigation("javascript:alert(1)")); assert(!is_safe_navigation("https://user:pass@example.com"));
 EventTarget t;int called=0;t.add_event_listener("click",[&](Event&e){called++;e.default_prevented=true;});Event e{"click"};assert(!t.dispatch_event(e)&&called==1);
}
