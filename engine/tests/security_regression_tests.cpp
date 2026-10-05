#include "algerium/security.h"
#include "algerium/navigation.h"
#include "algerium/events.h"
#include "algerium/cookies.h"
#include <cassert>
#include <iostream>
using namespace algerium;
int main(){
 assert(same_origin(parse_origin("https://example.com/a"),parse_origin("https://example.com/b")));
 assert(!same_origin(parse_origin("https://example.com"),parse_origin("http://example.com")));
 assert(!same_origin(parse_origin("https://example.com"),parse_origin("https://evil.example")));
 assert(is_safe_navigation("https://example.com"));
 assert(!is_safe_navigation("javascript:alert(1)"));
 assert(!is_safe_navigation("https://user:pass@example.com/"));
 assert(!is_safe_navigation("http://127.0.0.1/")); assert(!is_safe_navigation("https://example.com:notaport/"));
 assert(is_private_host("192.168.1.1")); assert(is_private_host("::1")); assert(is_private_host("fd00::1")); assert(!is_private_host("8.8.8.8"));
 EventTarget e; bool ran=false, prevented=false;
 e.add_event_listener("submit",[&](Event& ev){ran=true;ev.prevent_default();});
 Event ev; e.dispatch_event("submit",ev); prevented=ev.default_prevented;
 assert(ran&&prevented);
 CookieJar jar; assert(jar.set_cookie("sid=abc; Path=/; Secure; HttpOnly","https://example.com/a",100)); assert(jar.cookie_header("https://example.com/b",101)=="sid=abc"); assert(jar.cookie_header("http://example.com/b",101)=="");
 SessionHistory h; h.navigate("https://a.example");h.navigate("https://b.example");assert(h.back()=="https://a.example");assert(h.forward()=="https://b.example");
 std::cout<<"security/navigation regressions passed\n";
}