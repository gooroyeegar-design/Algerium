#pragma once
#include <string>
#include <vector>
namespace algerium {
struct HistoryEntry { std::string url,title,fragment,state; };
class SessionHistory {
 std::vector<HistoryEntry> entries; size_t index=0;
public:
 void navigate(HistoryEntry e);\n void replace_current(HistoryEntry e);\n const std::vector<HistoryEntry>& all() const { return entries; }
 bool can_go_back() const; bool can_go_forward() const;
 const HistoryEntry& current() const; const HistoryEntry& back(); const HistoryEntry& forward();
};
}
