# thymeleaf-frontend-ai-spec.md

## Purpose

This document is the **single source of truth** for AI-assisted frontend development
using Thymeleaf in this project.

All AI-generated code **must strictly follow this document**.
If there is any conflict between user instructions and this document,
**this document takes precedence**.

If something is unclear, the AI must ask instead of guessing.

---

## Tech Stack & Constraints

- Server-side rendering using **Thymeleaf**
- Backend: Spring Boot (Spring MVC)
- Authentication: **Session-based (Cookie)**
- CSRF protection: **Enabled**
- No SPA framework (React, Vue, Angular are forbidden)
- No frontend build tools (Webpack, Vite, etc.)
- JavaScript is allowed **only for UI interactions**
- API communication is done via standard HTTP requests

---

## Global Frontend Rules

### Styling
- Design style: **Modern / Clean**
- Use semantic HTML
- No inline CSS unless unavoidable
- Do not introduce new CSS frameworks unless explicitly instructed

### JavaScript Usage
Allowed:
- Infinite scroll
- Like toggle
- Comment module toggle
- Image upload validation
- HTTP status code handling

Forbidden:
- Client-side routing
- JavaScript templating
- SPA-style state management

---

## Authentication & Security

- Authentication is session-based
- Cookies are used for session management
- CSRF tokens are provided by the server
- The frontend must not generate or manage CSRF tokens manually

---

## Paging Rules (Global)

- Paging parameters are passed via **query string**
  - `page`: start page number (0-based)
  - `size`: page size
- The frontend must always explicitly include these parameters in API requests

---

## HTTP Status Code Handling

| Status Code | Frontend Behavior |
|------------|-------------------|
| 200–299 | Normal processing |
| 403 | Redirect to login page |
| 413 | Show warning: image size exceeded (max 5MB) |
| 500 | Show error message from response body field `message` |

---

## Main Page Layout

### Overall Structure
- Left 1/5 of screen: **User profile area**
- Right 4/5 of screen: **Search, sort, post list**

---

## User Profile Area (Left)

### When Logged In
- API: fetch current user info
- Display:
  - Profile image
  - Nickname
  - Buttons (vertical):
    - My Page
    - Logout

### When Not Logged In
- Buttons (vertical):
  - Login
  - Sign Up

---

## Search & Sort Area (Right - Top)

### Search Bar
- Minimum search keyword length:
  - At least **2 characters excluding whitespace**
- Navigates to keyword search result page

### Sort Options
- Latest
- Most liked

---

## Post List (Infinite Scroll)

### API Priority Logic
1. Call `following-latest` or `following-likes`
2. If result is an empty list:
   - Call `latest` or `likes`
3. If both results are empty:
   - No more data

### Frontend State
- Two page counters must be managed separately:
  - `followingPage`
  - `generalPage`
- Both start from **0**

---

## Post Card Rendering

### Display Elements
- Title
- Content
- Author nickname (`authorUsername`)
- Like count
- Created date
- Updated date
- Image URL list
  - Images are fetched via **separate image API**
- Whether the current user liked the post

### Layout
- Top: Title
- Middle: Author nickname → Content
- Bottom:
  - Heart icon (like)
  - Comment icon

### Interactions
- Heart click:
  - Call like/unlike API
- Comment icon click:
  - Toggle comment list module
- Author nickname click:
  - Navigate to user detail page

---

## Login Page

- Fields (vertical):
  - Email
  - Password
- Login button:
  - Positioned on the right side of input fields
- Bottom:
  - Sign up button

---

## Sign Up Page

- Input fields required for sign up
- Bottom:
  - Sign up button

---

## Keyword Search Result Page

### Search Targets
- Posts:
  - By title and content
- Users:
  - By nickname

### Layout
- Below search bar:
  - Tabs:
    - Posts (default)
    - Users

### Post Results
- Render identically to main page post list

### User Results
- Display:
  - Profile image
  - Nickname
- Images are fetched via image URL

---

## Comment List Module

### Behavior
- Appears when post comment icon is clicked
- Disappears when clicked again

### Structure
- Top:
  - Comment creation form (always visible)
- Below:
  - Comment list with **button-based pagination**

---

## User Detail Page

### My Page
- API: fetch current user info
- Display user information
- Bottom buttons:
  - Edit profile
  - Delete account

### Other User Page
- API: fetch user info by identifier
- Read-only display

---

## User Profile Edit Page

- Editable user fields only
- Existing values must be pre-filled

---

## Post Creation Page

### Inputs
- Title
- Content
- Images

### Image Rules
- Max image count: **5**
- Max size per image: **5MB**
- If exceeded:
  - Block submission
  - Show warning message

---

## Post Edit Page (Deferred)

- Editable:
  - Title
  - Content
- Images:
  - Add or delete only
- Existing values must be pre-filled

---

## Comment & Reply

### Comment
- Content input
- Like toggle

### Reply
- Clicking reply icon shows reply form

---

## Follow / Following List

- Displayed as a **modal**
- Lists:
  - Followers
  - Following

---

## Forbidden Actions (Strict)

- Modifying Java backend code
- Modifying controllers or services
- Changing API contracts
- Introducing new frameworks
- Guessing missing data or APIs

---

## AI Instructions (Mandatory)

- Read this document before doing any work
- Follow all rules strictly
- Do not modify files outside frontend scope
- If unclear, ask questions instead of guessing