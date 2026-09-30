import { Route, Routes } from 'react-router-dom';
import HomePage from './pages/HomePage';
import ResultPage from './pages/ResultPage';
import RecipeDetailPage from './pages/RecipeDetailPage';

export default function App() {
  return (
    <div className="min-h-screen bg-orange-50 text-stone-800">
      <div className="mx-auto max-w-md px-4 py-6">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/result" element={<ResultPage />} />
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        </Routes>
      </div>
    </div>
  );
}
