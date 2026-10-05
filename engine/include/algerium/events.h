#pragma once
#include <functional>
#include <string>
#include <unordered_map>
#include <vector>
namespace algerium {
struct Event { std::string type; bool bubbles=true; bool cancelable=true; bool default_prevented=false; bool propagation_stopped=false; void prevent_default(){if(cancelable)default_prevented=true;} void stop_propagation(){propagation_stopped=true;} };
class EventTarget {
 std::unordered_map<std::string,std::vector<std::function<void(Event&)>>> listeners;
public:
 void add_event_listener(const std::string&type,std::function<void(Event&)> cb);
 bool dispatch_event(Event e);\n void remove_event_listener(const std::string&type);
};
}
